package v1alpha1

import (
	"github.com/RHsyseng/operator-utils/pkg/olm"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// EDIT THIS FILE!  THIS IS SCAFFOLDING FOR YOU TO OWN!
// NOTE: json tags are required.  Any new fields you add must have json tags for the fields to be serialized.

// ServerlessOrchestrationAppSpec defines the desired state of ServerlessOrchestrationApp
// +k8s:openapi-gen=true
type ServerlessOrchestrationAppSpec struct {
	Name       string                 `json:"name"`
	Definition string                 `json:"definition"`
	Image      string                 `json:"image"`
	Ports      []corev1.ContainerPort `json:"ports"`
}

// ServerlessOrchestrationAppStatus defines the observed state of ServerlessOrchestrationApp
// +k8s:openapi-gen=true
type ServerlessOrchestrationAppStatus struct {
	Deployments olm.DeploymentStatus `json:"deployments"`
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

// ServerlessOrchestrationApp is the Schema for the serverlessorchestrationapps API
// +k8s:openapi-gen=true
// +kubebuilder:subresource:status
type ServerlessOrchestrationApp struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   ServerlessOrchestrationAppSpec   `json:"spec,omitempty"`
	Status ServerlessOrchestrationAppStatus `json:"status,omitempty"`
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

// ServerlessOrchestrationAppList contains a list of ServerlessOrchestrationApp
type ServerlessOrchestrationAppList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []ServerlessOrchestrationApp `json:"items"`
}

func init() {
	SchemeBuilder.Register(&ServerlessOrchestrationApp{}, &ServerlessOrchestrationAppList{})
}
