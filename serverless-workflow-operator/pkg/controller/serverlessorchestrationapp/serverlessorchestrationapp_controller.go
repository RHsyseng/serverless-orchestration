package serverlessorchestrationapp

import (
	"context"
	"github.com/RHsyseng/operator-utils/pkg/olm"
	"github.com/go-logr/logr"
	appv1alpha1 "github.com/kiegroup/serverless-orchestration-operator/pkg/apis/app/v1alpha1"
	oappsv1 "github.com/openshift/api/apps/v1"
	routev1 "github.com/openshift/api/route/v1"
	corev1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/apimachinery/pkg/util/intstr"
	cachev1 "sigs.k8s.io/controller-runtime/pkg/cache"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/controller"
	"sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
	"sigs.k8s.io/controller-runtime/pkg/handler"
	"sigs.k8s.io/controller-runtime/pkg/manager"
	"sigs.k8s.io/controller-runtime/pkg/reconcile"
	logf "sigs.k8s.io/controller-runtime/pkg/runtime/log"
	"sigs.k8s.io/controller-runtime/pkg/source"
)

var log = logf.Log.WithName("controller_serverlessorchestrationapp")

/**
* USER ACTION REQUIRED: This is a scaffold file intended for the user to modify with their own Controller
* business logic.  Delete these comments after modifying this file.*
 */

// Add creates a new ServerlessOrchestrationApp Controller and adds it to the Manager. The Manager will set fields on the Controller
// and Start it when the Manager is Started.
func Add(mgr manager.Manager) error {
	return add(mgr, newReconciler(mgr))
}

// newReconciler returns a new reconcile.Reconciler
func newReconciler(mgr manager.Manager) reconcile.Reconciler {
	return &ReconcileServerlessOrchestrationApp{client: mgr.GetClient(), scheme: mgr.GetScheme(), cache: mgr.GetCache()}
}

// add adds a new Controller to mgr with r as the reconcile.Reconciler
func add(mgr manager.Manager, r reconcile.Reconciler) error {
	// Create a new controller
	c, err := controller.New("serverlessorchestrationapp-controller", mgr, controller.Options{Reconciler: r})
	if err != nil {
		return err
	}

	// Watch for changes to primary resource ServerlessOrchestrationApp
	err = c.Watch(&source.Kind{Type: &appv1alpha1.ServerlessOrchestrationApp{}}, &handler.EnqueueRequestForObject{})
	if err != nil {
		return err
	}

	watchOwnedObjects := []runtime.Object{
		&oappsv1.DeploymentConfig{},
		&corev1.Service{},
		&routev1.Route{},
	}
	ownerHandler := &handler.EnqueueRequestForOwner{
		IsController: true,
		OwnerType:    &appv1alpha1.ServerlessOrchestrationApp{},
	}
	for _, watchObject := range watchOwnedObjects {
		err = c.Watch(&source.Kind{Type: watchObject}, ownerHandler)
		if err != nil {
			return err
		}
	}

	return nil
}

// blank assignment to verify that ReconcileServerlessOrchestrationApp implements reconcile.Reconciler
var _ reconcile.Reconciler = &ReconcileServerlessOrchestrationApp{}

// ReconcileServerlessOrchestrationApp reconciles a ServerlessOrchestrationApp object
type ReconcileServerlessOrchestrationApp struct {
	// This client, initialized using mgr.Client() above, is a split client
	// that reads objects from the cache and writes to the apiserver
	client client.Client
	scheme *runtime.Scheme
	cache  cachev1.Cache
}

// Reconcile reads that state of the cluster for a ServerlessOrchestrationApp object and makes changes based on the state read
// and what is in the ServerlessOrchestrationApp.Spec
// Note:
// The Controller will requeue the Request to be processed again if the returned error is non-nil or
// Result.Requeue is true, otherwise upon completion it will remove the work from the queue.
func (r *ReconcileServerlessOrchestrationApp) Reconcile(request reconcile.Request) (reconcile.Result, error) {
	reqLogger := log.WithValues("Request.Namespace", request.Namespace, "Request.Name", request.Name)
	reqLogger.Info("Reconciling ServerlessOrchestrationApp")

	// Fetch the ServerlessOrchestrationApp instance
	instance := &appv1alpha1.ServerlessOrchestrationApp{}
	err := r.client.Get(context.TODO(), request.NamespacedName, instance)
	if err != nil {
		if errors.IsNotFound(err) {
			// Request object not found, could have been deleted after reconcile request.
			// Owned objects are automatically garbage collected. For additional cleanup logic use finalizers.
			// Return and don't requeue
			return reconcile.Result{}, nil
		}
		// Error reading the object - requeue the request.
		return reconcile.Result{}, err
	}

	// Define a CM
	cm := newCMForCR(instance)
	cmObjectForCR := &objectForCR{runtimeObject: cm, metaObject: cm, objectMeta: &cm.ObjectMeta, objectType: "ConfigMap"}
	if err = r.createObjectForCR(instance, cmObjectForCR, &corev1.ConfigMap{}, reqLogger); err != nil {
		return reconcile.Result{}, err
	}

	// Define a DC
	dc := newDCForCR(instance, cm)
	dcObjectForCR := &objectForCR{runtimeObject: dc, metaObject: dc, objectMeta: &dc.ObjectMeta, objectType: "DeploymentConfig"}
	if err = r.createObjectForCR(instance, dcObjectForCR, &oappsv1.DeploymentConfig{}, reqLogger); err != nil {
		return reconcile.Result{}, err
	}

	// Define a Service
	service := newServiceForCR(instance, dc)
	svcObjectForCR := &objectForCR{runtimeObject: service, metaObject: service, objectMeta: &service.ObjectMeta, objectType: "Service"}
	if err = r.createObjectForCR(instance, svcObjectForCR, &corev1.Service{}, reqLogger); err != nil {
		return reconcile.Result{}, err
	}

	// Define a Route
	route := newRouteForCR(instance, service)
	rtObjectForCR := &objectForCR{runtimeObject: route, metaObject: route, objectMeta: &route.ObjectMeta, objectType: "Route"}
	if err = r.createObjectForCR(instance, rtObjectForCR, &routev1.Route{}, reqLogger); err != nil {
		return reconcile.Result{}, err
	}

	// Update DC status
	dcInstance := &oappsv1.DeploymentConfig{}
	if err = r.client.Get(context.TODO(), types.NamespacedName{Name: dc.Name, Namespace: dc.Namespace}, dcInstance); err == nil {
		instance.Status.Deployments = olm.GetDeploymentConfigStatus([]oappsv1.DeploymentConfig{*dcInstance})
	}

	return reconcile.Result{}, nil
}

type objectForCR struct {
	runtimeObject runtime.Object
	metaObject    metav1.Object
	objectMeta    *metav1.ObjectMeta
	objectType    string
}

func (r *ReconcileServerlessOrchestrationApp) createObjectForCR(cr *appv1alpha1.ServerlessOrchestrationApp,
	obj *objectForCR, foundObj runtime.Object, reqLogger logr.Logger) error {

	// Set ServerlessOrchestrationApp instance as the owner and controller
	if err := controllerutil.SetControllerReference(cr, obj.metaObject, r.scheme); err != nil {
		return err
	}

	// Check if this object already exists
	err := r.client.Get(context.TODO(), types.NamespacedName{Name: obj.objectMeta.Name, Namespace: obj.objectMeta.Namespace}, foundObj)
	if err != nil && errors.IsNotFound(err) {
		reqLogger.Info("Creating a new object", "Object Type", obj.objectType, "Namespace", obj.objectMeta.Namespace, "Name", obj.objectMeta.Name)
		err = r.client.Create(context.TODO(), obj.runtimeObject)
		if err != nil {
			return err
		}

		reqLogger.Info(obj.objectType + " created successfully")
	} else if err != nil {
		return err
	}

	return nil
}

const (
	workflowData = "workflow.json"
	workflowEnv  = "WORKFLOW_PATH"
	workflowPath = "/opt/workflow/"
	workflowDef  = "workflow-def"
)

// Create deployment config for the app
func newCMForCR(cr *appv1alpha1.ServerlessOrchestrationApp) *corev1.ConfigMap {
	return &corev1.ConfigMap{
		ObjectMeta: metav1.ObjectMeta{
			Name:      cr.Spec.Name + "-config",
			Namespace: cr.Namespace,
			Labels: map[string]string{
				"app": cr.Name,
			},
		},
		Data: map[string]string{
			workflowData: cr.Spec.Definition,
		},
	}
}

// Create deployment config for the app
func newDCForCR(cr *appv1alpha1.ServerlessOrchestrationApp, cm *corev1.ConfigMap) *oappsv1.DeploymentConfig {
	return &oappsv1.DeploymentConfig{
		ObjectMeta: metav1.ObjectMeta{
			Name:      cr.Spec.Name + "-dc",
			Namespace: cr.Namespace,
			Labels: map[string]string{
				"app": cr.Name,
			},
		},
		Spec: oappsv1.DeploymentConfigSpec{
			Strategy: oappsv1.DeploymentStrategy{
				Type: oappsv1.DeploymentStrategyTypeRolling,
			},
			Replicas: 1,
			Template: &corev1.PodTemplateSpec{
				ObjectMeta: metav1.ObjectMeta{
					Name:      cr.Spec.Name + "-pod",
					Namespace: cr.Namespace,
					Labels: map[string]string{
						"app": cr.Name,
					},
				},
				Spec: corev1.PodSpec{
					Containers: []corev1.Container{
						{
							Name:            cr.Spec.Name,
							Image:           cr.Spec.Image,
							ImagePullPolicy: corev1.PullAlways,
							Ports:           cr.Spec.Ports,
							Env: []corev1.EnvVar{
								{
									Name:  workflowEnv,
									Value: workflowPath + workflowData,
								},
							},
							VolumeMounts: []corev1.VolumeMount{
								{
									Name:      workflowDef,
									MountPath: workflowPath,
								},
							},
						},
					},
					ServiceAccountName: "serverless-orchestration-operator",
					Volumes: []corev1.Volume{
						{
							Name: workflowDef,
							VolumeSource: corev1.VolumeSource{
								ConfigMap: &corev1.ConfigMapVolumeSource{
									LocalObjectReference: corev1.LocalObjectReference{
										Name: cm.Name,
									},
									DefaultMode: func(i int32) *int32 { return &i }(420),
								},
							},
						},
					},
				},
			},
		},
	}
}

// Create service for the app
func newServiceForCR(cr *appv1alpha1.ServerlessOrchestrationApp, dc *oappsv1.DeploymentConfig) *corev1.Service {
	var ports []corev1.ServicePort
	for _, port := range dc.Spec.Template.Spec.Containers[0].Ports {
		ports = append(ports, corev1.ServicePort{
			Name:       port.Name,
			Protocol:   port.Protocol,
			Port:       port.ContainerPort,
			TargetPort: intstr.FromInt(int(port.ContainerPort)),
		},
		)
	}

	return &corev1.Service{
		ObjectMeta: metav1.ObjectMeta{
			Name:      cr.Spec.Name + "-service",
			Namespace: cr.Namespace,
			Labels: map[string]string{
				"app": cr.Name,
			},
		},
		Spec: corev1.ServiceSpec{
			Selector: dc.Spec.Selector,
			Type:     corev1.ServiceTypeClusterIP,
			Ports:    ports,
		},
	}
}

// Create droute for the app
func newRouteForCR(cr *appv1alpha1.ServerlessOrchestrationApp, service *corev1.Service) *routev1.Route {
	return &routev1.Route{
		ObjectMeta: metav1.ObjectMeta{
			Name:      cr.Spec.Name + "-route",
			Namespace: cr.Namespace,
			Labels: map[string]string{
				"app": cr.Name,
			},
		},
		Spec: routev1.RouteSpec{
			Port: &routev1.RoutePort{
				TargetPort: intstr.FromString("http"),
			},
			To: routev1.RouteTargetReference{
				Kind: "Service",
				Name: service.Name,
			},
		},
	}
}
