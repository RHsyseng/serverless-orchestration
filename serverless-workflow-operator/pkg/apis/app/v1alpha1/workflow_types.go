package v1alpha1

import (
	"github.com/RHsyseng/operator-utils/pkg/olm"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// EDIT THIS FILE!  THIS IS SCAFFOLDING FOR YOU TO OWN!
// NOTE: json tags are required.  Any new fields you add must have json tags for the fields to be serialized.

// WorkflowSpec defines the desired state of Workflow
// +k8s:openapi-gen=true
type WorkflowSpec struct {
	Definition Definition `json:"definition"`
	Image      string     `json:"image,omitempty"`
	Watch      bool       `json:"watch,omitempty"`
	Knative    bool       `json:"knative,omitempty"`
}

type Definition struct {
	Metadata map[string]string `json:"metadata,omitempty"`
	States   []State           `json:"states,omitempty"`
}

type State struct {
	Name       string            `json:"name"`
	Type       StateType         `json:"type"`
	Start      bool              `json:"start,omitempty"`
	Filter     Filter            `json:"filter,omitempty"`
	NextState  string            `json:"next-state,omitempty"`
	Choices    []Choice          `json:"choices,omitempty"`
	ActionMode ActionModeType    `json:"action-mode,omitempty"`
	Actions    []Action          `json:"actions,omitempty"`
	Metadata   map[string]string `json:"metadata,omitempty"`
}

type Choice struct {
	Path      string            `json:"path"`
	Value     string            `json:"value"`
	Operator  OperatorType      `json:"operator"`
	NextState string            `json:"next-state"`
	Metadata  map[string]string `json:"metadata,omitempty"`
}

type Action struct {
	Function Function          `json:"function"`
	Filter   Filter            `json:"filter,omitempty"`
	Metadata map[string]string `json:"metadata,omitempty"`
}

type Function struct {
	Name     string            `json:"name"`
	Metadata map[string]string `json:"metadata,omitempty"`
}

type StateType string

const (
	operationState StateType = "OPERATION"
	switchState    StateType = "SWITCH"
	endState       StateType = "END"
)

type ActionModeType string

const (
	sequential ActionModeType = "SEQUENTIAL"
	parallel   ActionModeType = "PARALLEL"
)

type OperatorType string

const (
	eq   OperatorType = "EQ"
	lt   OperatorType = "LT"
	lteq OperatorType = "LTEQ"
	gt   OperatorType = "GT"
	gteq OperatorType = "GTEQ"
)

type Filter struct {
	InputPath  string            `json:"input-path,omitempty"`
	ResultPath string            `json:"result-path,omitempty"`
	OutputPath string            `json:"output-path,omitempty"`
	Metadata   map[string]string `json:"metadata,omitempty"`
}

// WorkflowStatus defines the observed state of Workflow
// +k8s:openapi-gen=true
type WorkflowStatus struct {
	Deployments olm.DeploymentStatus `json:"deployments"`
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

// Workflow is the Schema for the workflows API
// +k8s:openapi-gen=true
// +kubebuilder:subresource:status
type Workflow struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   WorkflowSpec   `json:"spec,omitempty"`
	Status WorkflowStatus `json:"status,omitempty"`
}

// +k8s:deepcopy-gen:interfaces=k8s.io/apimachinery/pkg/runtime.Object

// WorkflowList contains a list of Workflow
type WorkflowList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []Workflow `json:"items"`
}

func init() {
	SchemeBuilder.Register(&Workflow{}, &WorkflowList{})
}
