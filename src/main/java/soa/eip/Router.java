package soa.eip;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.aggregate.AggregationStrategy;
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
		from("direct:search").setExchangePattern(ExchangePattern.InOut).split(body().tokenize(";"), new MyOrderStrategy()).process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				String q = exchange.getIn().getBody(String.class);
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
				+ "accessTokenSecret={{twitter.accessTokenSecret}}").log("${body}");
	}

    public static class MyOrderStrategy implements AggregationStrategy {

        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
            // put order together in old exchange by adding the order from new exchange

            if (oldExchange == null) {
                // the first time we aggregate we only have the new exchange,
                // so we just return it
                return newExchange;
            }

            String orders = oldExchange.getIn().getBody(String.class);
            String newLine = newExchange.getIn().getBody(String.class);

            if (newLine.length()>1) {
                orders = orders.substring(0,orders.length()-1) + ", "+newLine.substring(1);
            }
            // put combined order back on old to preserve it
            oldExchange.getIn().setBody(orders);

            // return old as this is the one that has all the orders gathered until now
            return oldExchange;
        }
    }
}
