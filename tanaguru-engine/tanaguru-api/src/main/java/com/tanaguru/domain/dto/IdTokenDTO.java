package com.tanaguru.domain.dto;

import java.util.Base64;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.exception.CustomInvalidArgumentException;
import com.tanaguru.service.impl.UserServiceImpl;

public class IdTokenDTO {
	
    private final Logger LOGGER = LoggerFactory.getLogger(IdTokenDTO.class);

	private PayloadDTO payload;
	
	private JoseHeaderDTO joseHeader;
	
	private String signature;

	public IdTokenDTO(String idTokenEncoded) {
		ObjectMapper objectMapper = new ObjectMapper();
	    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		String[] parts = idTokenEncoded.split(Pattern.quote("."));
		if(parts.length == 3) {
			this.signature = parts[2];
			try {
				this.payload = objectMapper.readValue(new String(Base64.getDecoder().decode(parts[1])), PayloadDTO.class);
				this.joseHeader = objectMapper.readValue(new String(Base64.getDecoder().decode(parts[0])), JoseHeaderDTO.class);
			} catch (JsonProcessingException e) {
				throw new CustomInvalidArgumentException(CustomError.SSO_JSON_PROCESSING_PAYLOAD_JOSEHEADER, e.getMessage());
			}
		}else {
			throw new CustomInvalidArgumentException(CustomError.SSO_ID_TOKEN_MALFORMED);
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
