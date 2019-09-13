package controller

import (
	"github.com/kiegroup/serverless-orchestration-operator/pkg/controller/serverlessorchestrationapp"
)

func init() {
	// AddToManagerFuncs is a list of functions to create controllers and add them to a manager.
	AddToManagerFuncs = append(AddToManagerFuncs, serverlessorchestrationapp.Add)
}
