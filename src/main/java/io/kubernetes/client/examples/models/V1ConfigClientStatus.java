/*
 * Kubernetes
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: v1.18.2
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package io.kubernetes.client.examples.models;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * ConfigClientStatus defines the observed state of ConfigClient
 */
@ApiModel(description = "ConfigClientStatus defines the observed state of ConfigClient")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen",
		date = "2020-12-04T15:50:40.049Z[Etc/UTC]")
public class V1ConfigClientStatus {

	public static final String SERIALIZED_NAME_COMPLETE = "complete";

	@SerializedName(SERIALIZED_NAME_COMPLETE)
	private Boolean complete;

	public static final String SERIALIZED_NAME_OBSERVED_GENERATION = "observedGeneration";

	@SerializedName(SERIALIZED_NAME_OBSERVED_GENERATION)
	private Long observedGeneration;

	public V1ConfigClientStatus complete(Boolean complete) {

		this.complete = complete;
		return this;
	}

	/**
	 * Get complete
	 * @return complete
	 **/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public Boolean getComplete() {
		return complete;
	}

	public void setComplete(Boolean complete) {
		this.complete = complete;
	}

	public V1ConfigClientStatus observedGeneration(Long observedGeneration) {

		this.observedGeneration = observedGeneration;
		return this;
	}

	/**
	 * Get observedGeneration
	 * @return observedGeneration
	 **/
	@javax.annotation.Nullable
	@ApiModelProperty(value = "")

	public Long getObservedGeneration() {
		return observedGeneration;
	}

	public void setObservedGeneration(Long observedGeneration) {
		this.observedGeneration = observedGeneration;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		V1ConfigClientStatus v1ConfigClientStatus = (V1ConfigClientStatus) o;
		return Objects.equals(this.complete, v1ConfigClientStatus.complete)
				&& Objects.equals(this.observedGeneration, v1ConfigClientStatus.observedGeneration);
	}

	@Override
	public int hashCode() {
		return Objects.hash(complete, observedGeneration);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class V1ConfigClientStatus {\n");
		sb.append("    complete: ").append(toIndentedString(complete)).append("\n");
		sb.append("    observedGeneration: ").append(toIndentedString(observedGeneration)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces (except the
	 * first line).
	 */
	private String toIndentedString(Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}

}
