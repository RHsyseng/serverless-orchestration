// +build !ignore_autogenerated

// This file was autogenerated by openapi-gen. Do not edit it manually!

package v1alpha1

import (
	spec "github.com/go-openapi/spec"
	common "k8s.io/kube-openapi/pkg/common"
)

func GetOpenAPIDefinitions(ref common.ReferenceCallback) map[string]common.OpenAPIDefinition {
	return map[string]common.OpenAPIDefinition{
		"./pkg/apis/app/v1alpha1.ServerlessOrchestrationApp":       schema_pkg_apis_app_v1alpha1_ServerlessOrchestrationApp(ref),
		"./pkg/apis/app/v1alpha1.ServerlessOrchestrationAppSpec":   schema_pkg_apis_app_v1alpha1_ServerlessOrchestrationAppSpec(ref),
		"./pkg/apis/app/v1alpha1.ServerlessOrchestrationAppStatus": schema_pkg_apis_app_v1alpha1_ServerlessOrchestrationAppStatus(ref),
	}
}

func schema_pkg_apis_app_v1alpha1_ServerlessOrchestrationApp(ref common.ReferenceCallback) common.OpenAPIDefinition {
	return common.OpenAPIDefinition{
		Schema: spec.Schema{
			SchemaProps: spec.SchemaProps{
				Description: "ServerlessOrchestrationApp is the Schema for the serverlessorchestrationapps API",
				Properties: map[string]spec.Schema{
					"kind": {
						SchemaProps: spec.SchemaProps{
							Description: "Kind is a string value representing the REST resource this object represents. Servers may infer this from the endpoint the client submits requests to. Cannot be updated. In CamelCase. More info: https://git.k8s.io/community/contributors/devel/api-conventions.md#types-kinds",
							Type:        []string{"string"},
							Format:      "",
						},
					},
					"apiVersion": {
						SchemaProps: spec.SchemaProps{
							Description: "APIVersion defines the versioned schema of this representation of an object. Servers should convert recognized schemas to the latest internal value, and may reject unrecognized values. More info: https://git.k8s.io/community/contributors/devel/api-conventions.md#resources",
							Type:        []string{"string"},
							Format:      "",
						},
					},
					"metadata": {
						SchemaProps: spec.SchemaProps{
							Ref: ref("k8s.io/apimachinery/pkg/apis/meta/v1.ObjectMeta"),
						},
					},
					"spec": {
						SchemaProps: spec.SchemaProps{
							Ref: ref("./pkg/apis/app/v1alpha1.ServerlessOrchestrationAppSpec"),
						},
					},
					"status": {
						SchemaProps: spec.SchemaProps{
							Ref: ref("./pkg/apis/app/v1alpha1.ServerlessOrchestrationAppStatus"),
						},
					},
				},
			},
		},
		Dependencies: []string{
			"./pkg/apis/app/v1alpha1.ServerlessOrchestrationAppSpec", "./pkg/apis/app/v1alpha1.ServerlessOrchestrationAppStatus", "k8s.io/apimachinery/pkg/apis/meta/v1.ObjectMeta"},
	}
}

func schema_pkg_apis_app_v1alpha1_ServerlessOrchestrationAppSpec(ref common.ReferenceCallback) common.OpenAPIDefinition {
	return common.OpenAPIDefinition{
		Schema: spec.Schema{
			SchemaProps: spec.SchemaProps{
				Description: "ServerlessOrchestrationAppSpec defines the desired state of ServerlessOrchestrationApp",
				Properties: map[string]spec.Schema{
					"name": {
						SchemaProps: spec.SchemaProps{
							Type:   []string{"string"},
							Format: "",
						},
					},
					"definition": {
						SchemaProps: spec.SchemaProps{
							Type:   []string{"string"},
							Format: "",
						},
					},
					"image": {
						SchemaProps: spec.SchemaProps{
							Type:   []string{"string"},
							Format: "",
						},
					},
					"ports": {
						SchemaProps: spec.SchemaProps{
							Type: []string{"array"},
							Items: &spec.SchemaOrArray{
								Schema: &spec.Schema{
									SchemaProps: spec.SchemaProps{
										Ref: ref("k8s.io/api/core/v1.ContainerPort"),
									},
								},
							},
						},
					},
				},
				Required: []string{"name", "definition", "image", "ports"},
			},
		},
		Dependencies: []string{
			"k8s.io/api/core/v1.ContainerPort"},
	}
}

func schema_pkg_apis_app_v1alpha1_ServerlessOrchestrationAppStatus(ref common.ReferenceCallback) common.OpenAPIDefinition {
	return common.OpenAPIDefinition{
		Schema: spec.Schema{
			SchemaProps: spec.SchemaProps{
				Description: "ServerlessOrchestrationAppStatus defines the observed state of ServerlessOrchestrationApp",
				Properties: map[string]spec.Schema{
					"deployments": {
						SchemaProps: spec.SchemaProps{
							Ref: ref("github.com/RHsyseng/operator-utils/pkg/olm.DeploymentStatus"),
						},
					},
				},
				Required: []string{"deployments"},
			},
		},
		Dependencies: []string{
			"github.com/RHsyseng/operator-utils/pkg/olm.DeploymentStatus"},
	}
}