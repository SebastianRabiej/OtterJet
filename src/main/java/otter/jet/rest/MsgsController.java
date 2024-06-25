package otter.jet.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import otter.jet.store.MessageStore;
import otter.jet.reader.ReadMessage;
import otter.jet.store.Filters;

@Controller
public class MsgsController {

    private static final String TEMPLATE_NAME = "msgs-page";
    private static final Logger LOG = LoggerFactory.getLogger(MsgsController.class);

    private final MessageStore messageStore;

    public MsgsController(MessageStore messageStore) {
        this.messageStore = messageStore;
    }

    @GetMapping("/msgs")
    public String page(
            @RequestParam(value = "subject", required = false, defaultValue = "") String subject,
            @RequestParam(value = "type", required = false, defaultValue = "") String type,
            @RequestParam(value = "bodyContent", required = false, defaultValue = "") String bodyContent,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        Filters filters = Filters.of(subject, type, bodyContent);
        List<ReadMessage> filteredMessages = messageStore.filter(filters, page, size);
        LOG.info("amount of read messages: " + filteredMessages.size());
        model.addAttribute("messages", filteredMessages);
        model.addAttribute("subject", subject);
        model.addAttribute("type", type);
        model.addAttribute("bodyContent", bodyContent);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return TEMPLATE_NAME;
    }
}
