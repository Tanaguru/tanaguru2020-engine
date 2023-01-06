package com.tanaguru.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import org.apache.commons.text.RandomStringGenerator;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import com.tanaguru.domain.JwtResponse;
import com.tanaguru.domain.constant.CustomError;
import com.tanaguru.domain.constant.EAppAccountType;
import com.tanaguru.domain.constant.EAppRole;
import com.tanaguru.domain.dto.AccessTokenDTO;
import com.tanaguru.domain.dto.IdTokenDTO;
import com.tanaguru.domain.entity.membership.user.User;
import com.tanaguru.domain.exception.CustomEntityNotFoundException;
import com.tanaguru.domain.exception.CustomForbiddenException;
import com.tanaguru.domain.exception.CustomInvalidArgumentException;
import com.tanaguru.repository.UserRepository;
import com.tanaguru.security.Oauth2JwtTokenUtil;
import com.tanaguru.service.TanaguruUserDetailsService;
import com.tanaguru.service.UserService;

@Service
public class OAuth2Service {

    @Value("${oauth2.userInfoUri}")
    private String oauth2UserInfoUri;

    @Value("${oauth2.tokenUri}")
    private String oauth2TokenUri;

    @Value("${oauth2.clientId}")
    private String oauth2ClientId;

    @Value("${oauth2.clientSecret}")
    private String oauth2ClientSecret;

    @Value("${oauth2.redirectUri}")
    private String oauth2RedirectUri;

    @Value("${oauth2.oidc.enabled}")
    private boolean oauth2OidcEnabled;

    @Value("${oauth2.oidc.jwk}")
    private String oauth2OidcJwk;
    
    @Value("${oauth2.oidc.issuer}")
    private String oauth2OidcIss;

    @Value("${oauth2.user.createContract}")
    private boolean userCreateContract;

    @Value("${oauth2.user.createIfNotFound}")
    private boolean createUserIfNotFound;

    private final UserService userService;
    private PasswordEncoder passwordEncoder;
    private final TanaguruUserDetailsService tanaguruUserDetailsService;
    private final Oauth2JwtTokenUtil oauth2JwtTokenUtil;
    private final UserRepository userRepository;
    private final Logger LOGGER = LoggerFactory.getLogger(OAuth2Service.class);

    @Autowired
    public OAuth2Service(UserService userService, PasswordEncoder passwordEncoder,
            TanaguruUserDetailsService tanaguruUserDetailsService, Oauth2JwtTokenUtil oauth2JwtTokenUtil,
            UserRepository userRepository) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.tanaguruUserDetailsService = tanaguruUserDetailsService;
        this.oauth2JwtTokenUtil = oauth2JwtTokenUtil;
        this.userRepository = userRepository;
    }

    /**
     * Create and return a new JWT Token for the authentication of the user with
     * information from the AccessToken/IdToken
     * 
     * @param accessTokenDTO
     * @return jwt token for authentication
     */
    public JwtResponse createJwtTokenFromAccessToken(AccessTokenDTO accessTokenDTO) {
        String token = "";
        if (oauth2OidcEnabled) {
            token = this.createJwtTokenWithOidc(accessTokenDTO);
        } else {
            token = this.createJwtTokenWithoutOidc(accessTokenDTO);
        }
        return new JwtResponse(token);
    }

    /**
     * Create jwt token for authentication with IdToken oidc activated
     * 
     * @param accessTokenDTO
     * @return jwt token for authentication
     * @throws JsonProcessingException
     */
    private String createJwtTokenWithOidc(AccessTokenDTO accessTokenDTO){
        String token = "";
        if (accessTokenDTO.getId_token() != null) {
            IdTokenDTO idTokenDTO = new IdTokenDTO(accessTokenDTO.getId_token());
            if (idTokenDTO.getPayload() != null && idTokenDTO.getPayload().getEmail() != null) {
                if (expirationTimeIdTokenIsValid(idTokenDTO) && algAndKidIdTokenIsValid(idTokenDTO)) {
                    if(checkIssuer(idTokenDTO)) {
                        token = this.tokenForUserWithEmail(idTokenDTO.getPayload().getEmail());
                    }
                } else {
                    throw new CustomInvalidArgumentException(CustomError.SSO_ALG_KID_JOSEHEADER_NOT_MATCHING);
                }
            } else {
                throw new CustomInvalidArgumentException(CustomError.SSO_ID_TOKEN_INCORRECT_DATA);
            }
        } else {
            throw new CustomInvalidArgumentException(CustomError.SSO_ID_TOKEN_NOT_FOUND);
        }
        return token;
    }

    /**
     * Create jwt token for authentication without oidc activated
     * 
     * @param accessTokenDTO
     * @return jwt token for authentication
     */
    private String createJwtTokenWithoutOidc(AccessTokenDTO accessTokenDTO) {
        String token = "";
        JSONObject userInfos = this.getUserInfos(accessTokenDTO);
        if (userInfos.has("email")) {
            token = this.tokenForUserWithEmail(userInfos.getString("email"));
        } else {
            throw new CustomInvalidArgumentException(CustomError.SSO_EMAIL_NOT_FOUND_USERINFO);
        }
        return token;
    }

    /**
     * Return jwt token for authentication and create or not the user with email in
     * params
     * 
     * @param email of the user to search or create
     * @return jwt token
     */
    private String tokenForUserWithEmail(String email) {
        String token = "";
        UserDetails userDetails = null;
        try {
            userDetails = tanaguruUserDetailsService.loadUserByEmail(email);
        } catch (UsernameNotFoundException e) {
            if (createUserIfNotFound) {
                createSimpleUserWithEmail(email);
                userDetails = tanaguruUserDetailsService.loadUserByEmail(email);
            } else {
                throw new CustomEntityNotFoundException(CustomError.USER_NOT_FOUND);
            }
        }
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new CustomEntityNotFoundException(CustomError.USER_NOT_FOUND));
            if (user.getAppRole().getName().equals(EAppRole.USER)) {
                token = oauth2JwtTokenUtil.generateToken(userDetails);
            } else {
                throw new CustomEntityNotFoundException(CustomError.USER_IS_ADMIN_OR_SUPERADMIN);
            }
        }
        return token;
    }

    /**
     * Post request to the token uri in order to return the access token
     * 
     * @param authorizationCode
     * @return access token
     */
    public AccessTokenDTO getAccessToken(String authorizationCode) {
        AccessTokenDTO accessTokenDTO = new AccessTokenDTO();
        try {
            URL weburl = new URL(oauth2TokenUri);
            String parameters = "client_id=" + oauth2ClientId + "&client_secret=" + oauth2ClientSecret + "&code="
                    + authorizationCode + "&grant_type=" + "authorization_code" + "&redirect_uri=" + oauth2RedirectUri;

            byte[] postDataBytes = parameters.getBytes("UTF-8");
            HttpURLConnection conn = (HttpURLConnection) weburl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);

            InputStream inputStream = new ByteArrayInputStream(conn.getInputStream().readAllBytes());
            String content = "";
            try (Reader reader = new InputStreamReader(inputStream)) {
                content = CharStreams.toString(reader);
            }
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            accessTokenDTO = objectMapper.readValue(content, AccessTokenDTO.class);

        } catch (IOException e) {
            throw new CustomInvalidArgumentException(CustomError.SSO_CANT_GET_ACCESS_TOKEN);
        }
        return accessTokenDTO;
    }

    /**
     * Check if the expiration time of the token is valid
     * 
     * @param idToken
     * @return true if the date has not already passed
     */
    private boolean expirationTimeIdTokenIsValid(IdTokenDTO idToken) {
        boolean timeIsValid = false;
        Date expirationTime = new java.util.Date((long) idToken.getPayload().getExp() * 1000);
        if (expirationTime.after(new Date())) {
            timeIsValid = true;
        } else {
            throw new CustomInvalidArgumentException(CustomError.SSO_EXPIRATION_TIME_ID_TOKEN_INVALID);
        }
        return timeIsValid;
    }

    /**
     * Check if the issuer in the payload is correct (same as the one in config)
     * @param idToken
     * @return
     */
    private boolean checkIssuer(IdTokenDTO idToken) {
        boolean issIsValid = false;
        if(idToken.getPayload().getIss().equals(this.oauth2OidcIss)) {
            issIsValid = true;
        }else {
            throw new CustomInvalidArgumentException(CustomError.SSO_ISSUER_NOT_MATCHING);
        }
        return issIsValid;
    }
    
    /**
     * Check if the data alg and kid in the Token Id are the same as those obtained
     * via the url /jwk of the oauth2 provider
     * 
     * @param idToken
     * @return boolean
     */
    private boolean algAndKidIdTokenIsValid(IdTokenDTO idToken) {
        boolean algAndKidValid = false;
        try {
            URL weburl = new URL(oauth2OidcJwk);
            HttpURLConnection conn;
            conn = (HttpURLConnection) weburl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            InputStream inputStream = new ByteArrayInputStream(conn.getInputStream().readAllBytes());
            String content = "";
            try (Reader reader = new InputStreamReader(inputStream)) {
                content = CharStreams.toString(reader);
            }
            JSONObject publicKeys = new JSONObject(content);
            String alg = publicKeys.getJSONArray("keys").getJSONObject(0).getString("alg");
            String kid = publicKeys.getJSONArray("keys").getJSONObject(0).getString("kid");
            if (alg.equals(idToken.getJoseHeader().getAlg()) && kid.equals(idToken.getJoseHeader().getKid())) {
                algAndKidValid = true;
            }
        } catch (IOException e) {
            throw new CustomInvalidArgumentException(CustomError.SSO_CANT_GET_ALG_KID);
        }
        return algAndKidValid;
    }

    /**
     * Create user with email with random password (and a contract only if specified
     * in properties)
     * 
     * @param email
     */
    private void createSimpleUserWithEmail(String email) {
        RandomStringGenerator generator = new RandomStringGenerator.Builder().withinRange('0', 'z').build();
        String pswd = generator.generate(10);
        userService.createUser(email, email, passwordEncoder.encode(pswd), EAppRole.USER, true, userCreateContract, null, null, EAppAccountType.DEFAULT);
    }

    /**
     * Request the userinfo api uri of the oauth2 provider in order to obtain user
     * information
     * 
     * @param accessTokenDTO
     * @return user information in json
     */
    private JSONObject getUserInfos(AccessTokenDTO accessTokenDTO) {
        JSONObject userInfos = new JSONObject();
        try {
            URL weburl = new URL(oauth2UserInfoUri);
            HttpURLConnection conn;
            conn = (HttpURLConnection) weburl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + accessTokenDTO.getAccess_token());

            InputStream inputStream = new ByteArrayInputStream(conn.getInputStream().readAllBytes());
            String content = "";
            try (Reader reader = new InputStreamReader(inputStream)) {
                content = CharStreams.toString(reader);
            }
            userInfos = new JSONObject(content);
        } catch (IOException e) {
            throw new CustomInvalidArgumentException(CustomError.SSO_CANT_GET_USER_INFO);
        }
        return userInfos;
    }
}