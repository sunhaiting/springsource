/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.reactive.resource;

import java.util.Collections;

import reactor.core.publisher.Mono;

import org.springframework.core.io.Resource;
import org.springframework.web.server.ServerWebExchange;

/**
 * A base class for a {@code ResourceTransformer} with an optional helper method
 * for resolving public links within a transformed resource.
 *
 * @author Rossen Stoyanchev
 * @author Brian Clozel
 * @since 5.0
 */
public abstract class ResourceTransformerSupport implements ResourceTransformer {

	private ResourceUrlProvider resourceUrlProvider;


	/**
	 * Configure a {@link ResourceUrlProvider} to use when resolving the public
	 * URL of links in a transformed resource (e.g. import links in a CSS file).
	 * This is required only for links expressed as full paths and not for
	 * relative links.
	 * @param resourceUrlProvider the URL provider to use
	 */
	public void setResourceUrlProvider(ResourceUrlProvider resourceUrlProvider) {
		this.resourceUrlProvider = resourceUrlProvider;
	}

	/**
	 * @return the configured {@code ResourceUrlProvider}.
	 */
	public ResourceUrlProvider getResourceUrlProvider() {
		return this.resourceUrlProvider;
	}


	/**
	 * A transformer can use this method when a resource being transformed
	 * contains links to other resources. Such links need to be replaced with the
	 * public facing link as determined by the resource resolver chain (e.g. the
	 * public URL may have a version inserted).
	 * @param resourcePath the path to a resource that needs to be re-written
	 * @param exchange the current exchange
	 * @param resource the resource being transformed
	 * @param transformerChain the transformer chain
	 * @return the resolved URL or null
	 */
	protected Mono<String> resolveUrlPath(String resourcePath, ServerWebExchange exchange,
			Resource resource, ResourceTransformerChain transformerChain) {

		if (resourcePath.startsWith("/")) {
			// full resource path
			ResourceUrlProvider urlProvider = getResourceUrlProvider();
			return (urlProvider != null ? urlProvider.getForRequestUrl(exchange, resourcePath) : null);
		}
		else {
			// try resolving as relative path
			return transformerChain.getResolverChain()
					.resolveUrlPath(resourcePath, Collections.singletonList(resource));
		}
	}

}
