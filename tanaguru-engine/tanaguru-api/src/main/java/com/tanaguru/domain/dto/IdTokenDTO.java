package com.tanaguru.domain.dto;

import java.util.Base64;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanaguru.service.impl.UserServiceImpl;

public class IdTokenDTO {
	
    private final Logger LOGGER = LoggerFactory.getLogger(IdTokenDTO.class);

	private PayloadDTO payload;
	
	private JoseHeaderDTO joseHeader;
	
	private String signature;

	public IdTokenDTO(String idTokenEncoded) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
	    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		String[] parts = idTokenEncoded.split(Pattern.quote("."));
		if(parts.length == 3) {
			this.signature = parts[2];
			try {
			    System.out.println("Payload : "+ new String(Base64.getDecoder().decode(parts[1])));
			    System.out.println("JoseHeader : "+ new String(Base64.getDecoder().decode(parts[0])));
				this.payload = objectMapper.readValue(new String(Base64.getDecoder().decode(parts[1])), PayloadDTO.class);
				this.joseHeader = objectMapper.readValue(new String(Base64.getDecoder().decode(parts[0])), JoseHeaderDTO.class);
			} catch (JsonProcessingException e) {
			    LOGGER.error("Id Token malformed : payload or joseHeader decode Base64");
				throw e;
			}
		}else {
			LOGGER.error("Id Token malformed : 3 parts not presents");
		} 
	}
	
	public PayloadDTO getPayload() {
		return payload;
	}

	public void setPayload(PayloadDTO payload) {
		this.payload = payload;
	}

	public JoseHeaderDTO getJoseHeader() {
		return joseHeader;
	}

	public void setJoseHeader(JoseHeaderDTO joseHeader) {
		this.joseHeader = joseHeader;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	
}
