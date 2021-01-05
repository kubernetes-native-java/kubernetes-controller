/*
 * Copyright 2019-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.kubernetes.client.examples.reconciler;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import io.kubernetes.client.apimachinery.GroupVersion;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.extended.controller.reconciler.Reconciler;
import io.kubernetes.client.extended.controller.reconciler.Request;
import io.kubernetes.client.extended.controller.reconciler.Result;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.cache.Lister;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.generic.options.UpdateOptions;

import org.springframework.util.ReflectionUtils;

/**
 * @author Dave Syer
 *
 */
public class ParentReconciler<T extends KubernetesObject, L extends KubernetesListObject> implements Reconciler {

	private SharedIndexInformer<T> parentInformer;

	private SubReconciler<T>[] reconcilers;

	private ApiClient api;

	private String pluralName;

	public ParentReconciler(SharedIndexInformer<T> parentInformer, ApiClient api, List<SubReconciler<T>> reconcilers) {
		this(null, parentInformer, api, reconcilers);
	}

	public ParentReconciler(@Nullable String pluralName, SharedIndexInformer<T> parentInformer, ApiClient api,
			List<SubReconciler<T>> reconcilers) {
		this.parentInformer = parentInformer;
		this.pluralName = pluralName;
		this.api = api;
		@SuppressWarnings("unchecked")
		SubReconciler<T>[] array = (SubReconciler<T>[]) reconcilers.toArray();
		this.reconcilers = array;
	}

	@Override
	public Result reconcile(Request request) {
		Lister<T> parentLister = new Lister<>(parentInformer.getIndexer(), request.getNamespace());
		T parent = parentLister.get(request.getName());

		Result result = new Result(false);
		if (parent != null) {

			if (parent.getMetadata().getDeletionTimestamp() != null) {
				return result;
			}

			for (SubReconciler<T> subReconciler : reconcilers) {
				result = aggregate(subReconciler.reconcile(parent), result);
			}

			GroupVersion gv = GroupVersion.parse(parent);
			String pluralName = findPluralName(parent);
			@SuppressWarnings("unchecked")
			Class<T> apiType = (Class<T>) parent.getClass();
			GenericStatusWriter<T> status = new GenericStatusWriter<>(apiType, gv.getGroup(), gv.getVersion(),
					pluralName, this.api);

			// TODO: make this conditional on the status having changed
			if (!status.updateStatus(parent, this::extractStatus, new UpdateOptions()).isSuccess()) {
				throw new IllegalStateException("Cannot update parent");
			}

		}

		return result;
	}

	private String findPluralName(KubernetesObject parent) {
		if (this.pluralName != null) {
			return this.pluralName;
		}
		return parent.getKind().toLowerCase() + "s";
	}

	private Object extractStatus(KubernetesObject parent) {
		Method method = ReflectionUtils.findMethod(parent.getClass(), "getStatus");
		if (method != null) {
			Object status = ReflectionUtils.invokeMethod(method, parent);
			if (status != null) {
				return status;
			}
		}
		return Collections.emptyMap();
	}

	private Result aggregate(Result result, Result aggregate) {
		if (aggregate.getRequeueAfter() != null
				&& (aggregate.getRequeueAfter().isZero() || result.getRequeueAfter() != null
						&& aggregate.getRequeueAfter().compareTo(result.getRequeueAfter()) > 0)) {
			aggregate.setRequeueAfter(result.getRequeueAfter());
		}
		if (result.isRequeue()) {
			aggregate.setRequeue(true);
		}
		return aggregate;
	}

}
