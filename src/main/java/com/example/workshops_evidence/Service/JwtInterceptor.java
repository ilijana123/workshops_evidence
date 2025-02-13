package com.example.workshops_evidence.Service;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;

import java.util.Iterator;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

import javax.xml.namespace.QName;
import java.util.Iterator;

@Component
public class JwtInterceptor implements EndpointInterceptor {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Autowired
    public JwtInterceptor(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
        SoapMessage soapMessage = (SoapMessage) messageContext.getRequest();
        SoapHeader header = soapMessage.getSoapHeader();

        if (header == null) {
            throw new SecurityException("Missing SOAP Header");
        }

        Iterator<SoapHeaderElement> it = header.examineAllHeaderElements();
        String token = null;

        while (it.hasNext()) {
            SoapHeaderElement element = it.next();
            if ("Authorization".equals(element.getName().getLocalPart())) {
                token = element.getText();
                break;
            }
        }

        if (token == null || !token.startsWith("Bearer ")) {
            throw new SecurityException("Unauthorized: Missing or invalid token format");
        }

        String jwt = token.substring(7);
        String username = jwtService.extractUsername(jwt);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!jwtService.validateToken(jwt, userDetails)) {
            throw new SecurityException("Unauthorized: Invalid token");
        }

        SecurityContextHolder.getContext().setAuthentication(jwtService.getAuthentication(jwt));

        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        return true;
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) throws Exception {
    }
}
