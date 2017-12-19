package soa.web;

import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;


@Controller
public class SearchController {

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);

    @Autowired
    private ProducerTemplate producerTemplate;

    @RequestMapping("/")
    public String index() {
        return "index";
    }


    @RequestMapping(value="/search")
    @ResponseBody
    public Object search(@RequestParam("q") String q) {
        Map<String, Object> header = new HashMap<>();
        if (q.contains("max:")) {
            String n = q.substring(q.lastIndexOf("max:")+4);
            header.put("CamelTwitterKeywords",q.substring(0,q.lastIndexOf("max:")-1));
            header.put("CamelTwitterCount",n);
        } else {
            header.put("CamelTwitterKeywords",q);
        }
        return producerTemplate.requestBodyAndHeaders("direct:search", "",header);
    }
}