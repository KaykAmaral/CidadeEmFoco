package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.entity.WhatsappNotification;

public interface WhatsappMessageSender {

    String send(WhatsappNotification notification);
}
