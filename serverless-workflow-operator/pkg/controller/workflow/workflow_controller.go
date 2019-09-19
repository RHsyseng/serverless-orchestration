package workflow

import (
	"context"

	"github.com/RHsyseng/operator-utils/pkg/olm"
	v1 "github.com/RHsyseng/serverless-orchestration/serverless-workflow-operator/pkg/apis/app/v1alpha1"
	"github.com/go-logr/logr"
	appsv1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
	authv1 "k8s.io/api/rbac/v1"
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

var log = logf.Log.WithName("controller_workflow")

/**
* USER ACTION REQUIRED: This is a scaffold file intended for the user to modify with their own Controller
* business logic.  Delete these comments after modifying this file.*
 */

// Add creates a new Workflow Controller and adds it to the Manager. The Manager will set fields on the Controller
// and Start it when the Manager is Started.
func Add(mgr manager.Manager) error {
	return add(mgr, newReconciler(mgr))
}

// newReconciler returns a new reconcile.Reconciler
func newReconciler(mgr manager.Manager) reconcile.Reconciler {
	return &ReconcileWorkflow{client: mgr.GetClient(), scheme: mgr.GetScheme(), cache: mgr.GetCache()}
}

// add adds a new Controller to mgr with r as the reconcile.Reconciler
func add(mgr manager.Manager, r reconcile.Reconciler) error {
	// Create a new controller
	c, err := controller.New("workflow-controller", mgr, controller.Options{Reconciler: r})
	if err != nil {
		return err
	}

	// Watch for changes to primary resource Workflow
	err = c.Watch(&source.Kind{Type: &v1.Workflow{}}, &handler.EnqueueRequestForObject{})
	if err != nil {
		return err
	}

	watchOwnedObjects := []runtime.Object{
		&appsv1.Deployment{},
		&corev1.Service{},
	}
	ownerHandler := &handler.EnqueueRequestForOwner{
		IsController: true,
		OwnerType:    &v1.Workflow{},
	}
	for _, watchObject := range watchOwnedObjects {
		err = c.Watch(&source.Kind{Type: watchObject}, ownerHandler)
		if err != nil {
			return err
		}
	}

	return nil
}

// blank assignment to verify that ReconcileWorkflow implements reconcile.Reconciler
var _ reconcile.Reconciler = &ReconcileWorkflow{}

// ReconcileWorkflow reconciles a Workflow object
type ReconcileWorkflow struct {
	// This client, initialized using mgr.Client() above, is a split client
	// that reads objects from the cache and writes to the apiserver
	client client.Client
	scheme *runtime.Scheme
	cache  cachev1.Cache
}

// Reconcile reads that state of the cluster for a Workflow object and makes changes based on the state read
// and what is in the Workflow.Spec
// Note:
// The Controller will requeue the Request to be processed again if the returned error is non-nil or
// Result.Requeue is true, otherwise upon completion it will remove the work from the queue.
func (r *ReconcileWorkflow) Reconcile(request reconcile.Request) (reconcile.Result, error) {
	reqLogger := log.WithValues("Request.Namespace", request.Namespace, "Request.Name", request.Name)
	reqLogger.Info("Reconciling Workflow")

	// Fetch the Workflow instance
	instance := &v1.Workflow{}
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

	// Define a ServiceAccount
	sa := newServiceAccount(instance)
	saObjectForCR := &objectForCR{sa, sa, &sa.ObjectMeta, "ServiceAccount"}
	if err = r.createObjectForCR(instance, saObjectForCR, &corev1.ServiceAccount{}, reqLogger); err != nil {
		return reconcile.Result{}, err
	}

	// Define a RoleBinding
	rb := newRoleBinding(instance)
	rbObjectForCR := &objectForCR{rb, rb, &rb.ObjectMeta, "RoleBinding"}
	if err = r.createObjectForCR(instance, rbObjectForCR, &corev1.ServiceAccount{}, reqLogger); err != nil {
		return reconcile.Result{}, err
	}

	// Define a Deployment
	d := newDeploymentForCR(instance)
	dcObjectForCR := &objectForCR{runtimeObject: d, metaObject: d, objectMeta: &d.ObjectMeta, objectType: "Deployment"}
	if err = r.createObjectForCR(instance, dcObjectForCR, &appsv1.Deployment{}, reqLogger); err != nil {
		return reconcile.Result{}, err
	}

	// Define a Service
	service := newServiceForCR(instance)
	svcObjectForCR := &objectForCR{runtimeObject: service, metaObject: service, objectMeta: &service.ObjectMeta, objectType: "Service"}
	if err = r.createObjectForCR(instance, svcObjectForCR, &corev1.Service{}, reqLogger); err != nil {
		return reconcile.Result{}, err
	}

	// Update DC status
	dcInstance := &appsv1.Deployment{}
	if err = r.client.Get(context.TODO(), types.NamespacedName{Name: d.Name, Namespace: d.Namespace}, dcInstance); err == nil {
		instance.Status.Deployments = olm.GetDeploymentStatus([]appsv1.Deployment{*dcInstance})
	}

	return reconcile.Result{}, nil
}

type objectForCR struct {
	runtimeObject runtime.Object
	metaObject    metav1.Object
	objectMeta    *metav1.ObjectMeta
	objectType    string
}

func (r *ReconcileWorkflow) createObjectForCR(cr *v1.Workflow,
	obj *objectForCR, foundObj runtime.Object, reqLogger logr.Logger) error {

	// Set Workflow instance as the owner and controller
	if err := controllerutil.SetControllerReference(cr, obj.metaObject, r.scheme); err != nil {
		return err
	}

	err := r.client.Get(context.TODO(), types.NamespacedName{Name: obj.objectMeta.Name, Namespace: obj.objectMeta.Namespace}, foundObj)
	if err != nil && errors.IsNotFound(err) {
		reqLogger.Info("Creating a new object", "Object Type", obj.objectType, "Namespace", obj.objectMeta.Namespace, "Name", obj.objectMeta.Name)
		err = r.client.Create(context.TODO(), obj.runtimeObject)
		if err != nil {
			return err
		}
		reqLogger.Info(obj.objectType + " created successfully")
	} else {
		return err
	}
	return nil
}

const (
	_defaultImage = "quay.io/ruben/workflow-service:latest"
	_roleName     = "workflow-reader"
)

// Create ServiceAccount
func newServiceAccount(cr *v1.Workflow) *corev1.ServiceAccount {
	return &corev1.ServiceAccount{
		ObjectMeta: metav1.ObjectMeta{
			Name:      cr.Name,
			Namespace: cr.Namespace,
		},
	}
}

func newRoleBinding(cr *v1.Workflow) *authv1.RoleBinding {
	return &authv1.RoleBinding{
		ObjectMeta: metav1.ObjectMeta{
			Name:      cr.Name,
			Namespace: cr.Namespace,
		},
		Subjects: []authv1.Subject{
			{
				Kind: "ServiceAccount",
				Name: cr.Name,
			},
		},
		RoleRef: authv1.RoleRef{
			Kind: "Role",
			Name: _roleName,
		},
	}
}

// Create deployment for the app
func newDeploymentForCR(cr *v1.Workflow) *appsv1.Deployment {
	return &appsv1.Deployment{
		ObjectMeta: metav1.ObjectMeta{
			Name:      cr.Name,
			Namespace: cr.Namespace,
			Labels:    getLabels(cr),
		},
		Spec: appsv1.DeploymentSpec{
			Selector: &metav1.LabelSelector{
				MatchLabels: getDeploymentLabels(cr),
			},
			Strategy: appsv1.DeploymentStrategy{
				Type: appsv1.RollingUpdateDeploymentStrategyType,
			},
			Replicas: intPtr(1),
			Template: corev1.PodTemplateSpec{
				ObjectMeta: metav1.ObjectMeta{
					Name:      cr.Name,
					Namespace: cr.Namespace,
					Labels:    getDeploymentLabels(cr),
				},
				Spec: corev1.PodSpec{
					Containers: []corev1.Container{
						{
							Name:            cr.Name,
							Image:           getImage(cr),
							ImagePullPolicy: corev1.PullAlways,
							Ports: []corev1.ContainerPort{
								{
									Name:          "http",
									Protocol:      "TCP",
									ContainerPort: 8080,
								},
							},
							Env: []corev1.EnvVar{
								{
									Name:  "WORKFLOW_NAME",
									Value: cr.Name,
								},
								{
									Name:  "WORKFLOW_SOURCE",
									Value: "k8s",
								},
								{
									Name: "NAMESPACE",
									ValueFrom: &corev1.EnvVarSource{
										FieldRef: &corev1.ObjectFieldSelector{
											FieldPath: "metadata.namespace",
										},
									},
								},
							},
						},
					},
					ServiceAccountName: cr.Name,
				},
			},
		},
	}
}

// Create service for the app
func newServiceForCR(cr *v1.Workflow) *corev1.Service {
	ports := []corev1.ServicePort{
		{
			Name:       "http",
			Protocol:   "TCP",
			Port:       8080,
			TargetPort: intstr.FromInt(8080),
		},
	}

	return &corev1.Service{
		ObjectMeta: metav1.ObjectMeta{
			Name:      cr.Name,
			Namespace: cr.Namespace,
			Labels:    getLabels(cr),
		},
		Spec: corev1.ServiceSpec{
			Selector: getDeploymentLabels(cr),
			Type:     corev1.ServiceTypeClusterIP,
			Ports:    ports,
		},
	}
}

func getDeploymentLabels(cr *v1.Workflow) map[string]string {
	return map[string]string{
		"app":        cr.Name,
		"deployment": cr.Name,
	}
}

func getLabels(cr *v1.Workflow) map[string]string {
	return map[string]string{
		"app": cr.Name,
	}
}

func getImage(cr *v1.Workflow) string {
	if cr.Spec.Image == "" {
		return _defaultImage
	}
	return cr.Spec.Image
}

func intPtr(x int32) *int32 {
	return &x
}
