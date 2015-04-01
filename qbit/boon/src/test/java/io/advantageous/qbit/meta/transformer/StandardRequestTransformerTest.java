package io.advantageous.qbit.meta.transformer;

import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.meta.SampleService;
import io.advantageous.qbit.meta.builder.ContextMetaBuilder;
import io.advantageous.qbit.meta.provider.StandardMetaDataProvider;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static io.advantageous.boon.core.IO.puts;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StandardRequestTransformerTest {

    StandardRequestTransformer standardRequestTransformer;


    ContextMetaBuilder contextMetaBuilder;
    StandardMetaDataProvider provider;

    @Before
    public void setUp() throws Exception {


        contextMetaBuilder = ContextMetaBuilder.contextMetaBuilder();

        contextMetaBuilder.addService(SampleService.class);

        provider = new StandardMetaDataProvider(contextMetaBuilder.build());

        standardRequestTransformer = new StandardRequestTransformer(provider);
    }



    @Test
    public void testTransform() throws Exception {

        /*

                /services


        @RequestMapping("/sample/service")
        public class SampleService {


            @RequestMapping("/simple2/path/")
            public String simple2(@RequestParam("arg1") final String arg1) {
                return "simple2";
            }
         */

        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        requestBuilder.addParam("arg1", "" + 1);
        requestBuilder.setUri("/services/sample/service/simple2/path/");
        final HttpRequest request = requestBuilder.build();

        List<String> errorsList = new ArrayList<>();

        final MethodCall<Object> methodCall = standardRequestTransformer.transform(request, errorsList);

        assertEquals("simple2", methodCall.name());


        List<Object> args = (List<Object>) methodCall.body();

        assertEquals("1", args.get(0));
    }

    @Test
    public void testTransformComplex() throws Exception {

        /*

        /services


        @RequestMapping("/sample/service")
        public class SampleService {

            @RequestMapping("/call1/foo/{arg4}/{2}")
            public String method1(@RequestParam("arg1") final String arg1,
                                  @HeaderParam("arg2") final int arg2,
                                  @PathVariable final float arg3,
                                  @PathVariable("arg4") final double arg4) {
         */

        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        requestBuilder.addHeader("arg2", "" + 2);
        requestBuilder.addParam("arg1", "" + 1);
        requestBuilder.setUri("/services/sample/service/call1/foo/1.1/2.2");
        final HttpRequest request = requestBuilder.build();


        List<String> errorsList = new ArrayList<>();

        final MethodCall<Object> methodCall = standardRequestTransformer.transform(request, errorsList);
        assertNotNull(methodCall);
        assertEquals(0, errorsList.size());
        List<Object> args = (List<Object>) methodCall.body();
        assertEquals(4, args.size());
        assertEquals("1", args.get(0));
        assertEquals("2", args.get(1));
        assertEquals("2.2", args.get(2));
        assertEquals("1.1", args.get(3));


    }
}