package io.advantageous.qbit.service.rest.endpoint.tests;

import io.advantageous.boon.core.Sys;
import io.advantageous.boon.json.JsonFactory;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.http.request.HttpResponse;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.websocket.WebSocketMessage;
import io.advantageous.qbit.util.MultiMap;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class HttpServerSimulator implements HttpServer {

    private Consumer<HttpRequest> httpRequestConsumer;
    private Consumer<Void> idleConsumer;


    public final HttpResponse get(String uri) {

        final HttpRequestBuilder httpRequestBuilder = HttpRequestBuilder.httpRequestBuilder();
        httpRequestBuilder.setUri("/services" + uri);
        final AtomicReference<HttpResponse> response = getHttpResponseAtomicReference(httpRequestBuilder);

        return response.get();

    }


    public final HttpResponse postBody(String uri, Object object) {

        final HttpRequestBuilder httpRequestBuilder = HttpRequestBuilder.httpRequestBuilder();
        httpRequestBuilder.setMethodPost();
        httpRequestBuilder.setUri("/services" + uri);
        httpRequestBuilder.setBody(JsonFactory.toJson(object));
        final AtomicReference<HttpResponse> response = getHttpResponseAtomicReference(httpRequestBuilder);

        return response.get();

    }

    private void callService(final HttpRequest request) {

        httpRequestConsumer.accept(request);
        Sys.sleep(100);
        idleConsumer.accept(null);
    }

    private AtomicReference<HttpResponse> getHttpResponseAtomicReference(HttpRequestBuilder httpRequestBuilder) {
        final AtomicReference<HttpResponse> response = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);

        httpRequestBuilder.setTextReceiver((code, contentType, body) ->

                {

                    response.set(createResponse(code, contentType, body));
                    latch.countDown();
                }

        );

        Sys.sleep(100);

        callService(httpRequestBuilder.build());



        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return response;
    }

    private HttpResponse createResponse(int code, String contentType, String body) {
        return new HttpResponse() {

            @Override
            public MultiMap<String, String> headers() {
                return MultiMap.empty();
            }

            @Override
            public int code() {
                return code;
            }

            @Override
            public String contentType() {
                return contentType;
            }

            @Override
            public String body() {
                return body;
            }
        };
    }

    @Override
    public void setWebSocketMessageConsumer(Consumer<WebSocketMessage> webSocketMessageConsumer) {

    }

    @Override
    public void setWebSocketCloseConsumer(Consumer<WebSocketMessage> webSocketMessageConsumer) {

    }

    @Override
    public void setHttpRequestConsumer(Consumer<HttpRequest> httpRequestConsumer) {
        this.httpRequestConsumer = httpRequestConsumer;
    }

    @Override
    public void setHttpRequestsIdleConsumer(Consumer<Void> idleConsumer) {

        this.idleConsumer = idleConsumer;
    }

    @Override
    public void setWebSocketIdleConsume(Consumer<Void> idleConsumer) {

    }

    @Override
    public void start() {

    }
}
