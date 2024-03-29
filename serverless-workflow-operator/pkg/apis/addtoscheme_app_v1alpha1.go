package apis

import (
	"github.com/RHsyseng/serverless-orchestration/serverless-workflow-operator/pkg/apis/app/v1alpha1"
	knsv1 "knative.dev/serving/pkg/apis/serving/v1"
)

func init() {
	// Register the types with the Scheme so the components can map objects to GroupVersionKinds and back
	AddToSchemes = append(AddToSchemes,
		v1alpha1.SchemeBuilder.AddToScheme,
		knsv1.SchemeBuilder.AddToScheme,
	)
}
