package booternetes.k8s;

import io.kubernetes.client.extended.controller.Controller;
import io.kubernetes.client.extended.controller.builder.ControllerBuilder;
import io.kubernetes.client.extended.controller.reconciler.Reconciler;
import io.kubernetes.client.extended.controller.reconciler.Result;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.informer.cache.Lister;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.V1Node;
import io.kubernetes.client.openapi.models.V1NodeList;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.util.generic.GenericKubernetesApi;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author Dave Syer
 * @author Josh Long
 */

@Log4j2
@SpringBootApplication
public class KubernetesControllerApplication {

	public static void main(String[] args) {
		SpringApplication.run(KubernetesControllerApplication.class, args);
	}

	// ---------------------------INFORMER---------------------------
	//
	// Bean is an annotation. It's an object that is the returned object from the function
	// that spring created for us
	// using the function as the producer
	@Bean
	SharedIndexInformer<V1Node> nodeInformer(ApiClient apiClient, SharedInformerFactory sharedInformerFactory) {
		return sharedInformerFactory.sharedIndexInformerFor(
				new GenericKubernetesApi<>(V1Node.class, V1NodeList.class, "", "v1", "nodes", apiClient), V1Node.class,
				0);
	}

	@Bean
	SharedIndexInformer<V1Pod> podInformer(ApiClient apiClient, SharedInformerFactory sharedInformerFactory) {
		return sharedInformerFactory.sharedIndexInformerFor(
				new GenericKubernetesApi<>(V1Pod.class, V1PodList.class, "", "v1", "pods", apiClient), V1Pod.class, 0);
	}

	// ---------------------------LISTER---------------------------
	// Lists the current nodes and pods that it finds
	@Bean
	Lister<V1Node> nodeLister(SharedIndexInformer<V1Node> informer) {
		return new Lister<>(informer.getIndexer());
	}

	@Bean
	Lister<V1Pod> podLister(SharedIndexInformer<V1Pod> informer) {
		return new Lister<>(informer.getIndexer());
	}

	@Bean
	Reconciler reconciler(Lister<V1Node> nodeLister, Lister<V1Pod> podLister) {
		return request -> {
			var namespace = "bk";
			var node = nodeLister.get(request.getName());

			System.out.println("node: " + node.getMetadata().getName());

			podLister.namespace(namespace).list().stream().map(pod -> pod.getMetadata().getName())
					.forEach(podName -> System.out.println("pod name: " + podName));

			return new Result(false);
		};
	}

	// ---------------------------CONTROLLER---------------------------
	@Bean
	Controller controller(SharedIndexInformer<V1Pod> podInformer, SharedIndexInformer<V1Node> nodeInformer,
			SharedInformerFactory sharedInformerFactory, Reconciler reconciler) {

		return ControllerBuilder.defaultBuilder(sharedInformerFactory)
				.watch(q -> ControllerBuilder.controllerWatchBuilder(V1Node.class, q).build())
				.withReadyFunc(() -> podInformer.hasSynced() && nodeInformer.hasSynced()).withReconciler(reconciler)
				.withName("booternetesController").build();
	}

	@Bean
	CommandLineRunner go(SharedInformerFactory sharedInformerFactory, Controller controller) {
		return args -> {
			sharedInformerFactory.startAllRegisteredInformers();
			controller.run();
		};
	}

}
