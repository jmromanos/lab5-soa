package soa.eip;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import soa.web.SearchController;

import java.util.HashMap;
import java.util.Map;

@Component
public class Router extends RouteBuilder{
	private static final Logger log = LoggerFactory.getLogger(Router.class);

	@Override
	public void configure() throws Exception {
		from("direct:search").split(body().tokenize(";")).process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				String q = exchange.getIn().getBody(String.class);
				log.info(q);
				exchange.getIn().setBody("");
				Map<String, Object> header = new HashMap<>();
				if (q.contains("max:")) {
					String n = q.substring(q.lastIndexOf("max:")+4);
					header.put("CamelTwitterKeywords",q.substring(0,q.lastIndexOf("max:")-1));
					header.put("CamelTwitterCount",n);
				} else {
					header.put("CamelTwitterKeywords",q);
				}
				exchange.getIn().setHeaders(header);
			}
		}).to("twitter://search?consumerKey={{twitter.consumerKey}}&"
				+ "consumerSecret={{twitter.consumerSecret}}&"
				+ "accessToken={{twitter.accessToken}}&"
				+ "accessTokenSecret={{twitter.accessTokenSecret}}");
	}
}
