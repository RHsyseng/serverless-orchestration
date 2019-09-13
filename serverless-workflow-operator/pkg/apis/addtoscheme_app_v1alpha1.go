package apis

import (
	"github.com/kiegroup/serverless-orchestration-operator/pkg/apis/app/v1alpha1"
	oappsv1 "github.com/openshift/api/apps/v1"
	routev1 "github.com/openshift/api/route/v1"
)

func init() {
	// Register the types with the Scheme so the components can map objects to GroupVersionKinds and back
	AddToSchemes = append(AddToSchemes,
		v1alpha1.SchemeBuilder.AddToScheme,
		oappsv1.AddToScheme,
		routev1.AddToScheme,
	)
}
