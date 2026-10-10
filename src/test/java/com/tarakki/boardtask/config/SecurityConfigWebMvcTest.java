package com.tarakki.boardtask.config;

import com.tarakki.boardtask.controller.BoardController;
import com.tarakki.boardtask.dto.BoardDTO;
import com.tarakki.boardtask.service.BoardService;
import com.tarakki.boardtask.util.BoardTestDataFactory;
import com.tarakki.boardtask.util.JwtTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BoardController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "jwt.secret=" + JwtTestDataFactory.SECRET,
        "cors.allowed-origins=" + SecurityConfigWebMvcTest.ALLOWED_ORIGIN
})
class SecurityConfigWebMvcTest {

    static final String ALLOWED_ORIGIN = "http://localhost:5173";

    private static final String UNAUTHENTICATED_BODY = "Authentication required";

    // The Boot 4 WebMvc slice does not auto-configure Spring Security, so switch it on and
    // put the real filter chain in front of MockMvc explicitly.
    @TestConfiguration
    @EnableWebSecurity
    static class WebSecurityTestConfig {
    }

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BoardService boardService;

    private MockMvc mockMvc;
    private Long boardId;
    private UUID memberId;
    private BoardDTO board;
    private String protectedUrl;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        boardId = BoardTestDataFactory.EXISTING_BOARD_ID;
        memberId = UUID.randomUUID();
        board = BoardTestDataFactory.createBoardDTOWithId();
        protectedUrl = "/api/boards/" + boardId;
    }

    @Test
    void shouldAllowRequestWithValidToken() throws Exception {
        when(boardService.getBoardById(boardId)).thenReturn(board);

        mockMvc.perform(get(protectedUrl)
                        .header(HttpHeaders.AUTHORIZATION, bearer(JwtTestDataFactory.createValidToken(memberId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardId").value(board.getBoardId()));

        verify(boardService).getBoardById(boardId);
    }

    @Test
    void shouldCreateBoardAsTheMemberInTheTokenEvenWhenTheBodyNamesSomeoneElse() throws Exception {
        BoardDTO request = BoardTestDataFactory.createBoardDTO();
        request.setCreatedBy(UUID.randomUUID());
        when(boardService.createBoard(any())).thenReturn(board);

        mockMvc.perform(post("/api/boards")
                        .header(HttpHeaders.AUTHORIZATION, bearer(JwtTestDataFactory.createValidToken(memberId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        ArgumentCaptor<BoardDTO> sentToService = ArgumentCaptor.forClass(BoardDTO.class);
        verify(boardService).createBoard(sentToService.capture());
        assertEquals(memberId, sentToService.getValue().getCreatedBy());
    }

    @Test
    void shouldRejectCreateWhenTokenSubjectIsNotAMemberId() throws Exception {
        assertRejected(post("/api/boards")
                .header(HttpHeaders.AUTHORIZATION, bearer(JwtTestDataFactory.createValidTokenWithSubject("not-a-member-id")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(BoardTestDataFactory.createBoardDTO())));
    }

    @Test
    void shouldRejectRequestWithoutToken() throws Exception {
        assertRejected(get(protectedUrl));
    }

    @Test
    void shouldRejectUnknownPathWithoutToken() throws Exception {
        assertRejected(get("/api/does-not-exist"));
    }

    @Test
    void shouldReturnNotFoundForUnknownPathWithValidToken() throws Exception {
        mockMvc.perform(get("/api/does-not-exist")
                        .header(HttpHeaders.AUTHORIZATION, bearer(JwtTestDataFactory.createValidToken(memberId))))
                .andExpect(status().isNotFound());

        verifyNoInteractions(boardService);
    }

    @Test
    void shouldRejectRequestWithMalformedToken() throws Exception {
        assertRejected(get(protectedUrl).header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"));
    }

    @Test
    void shouldRejectRequestWithExpiredToken() throws Exception {
        assertRejected(get(protectedUrl)
                .header(HttpHeaders.AUTHORIZATION, bearer(JwtTestDataFactory.createExpiredToken(memberId))));
    }

    @Test
    void shouldRejectRequestWithTokenSignedByDifferentSecret() throws Exception {
        assertRejected(get(protectedUrl)
                .header(HttpHeaders.AUTHORIZATION, bearer(JwtTestDataFactory.createTokenSignedWithOtherSecret(memberId))));
    }

    @Test
    void shouldRejectRequestWithTamperedToken() throws Exception {
        assertRejected(get(protectedUrl)
                .header(HttpHeaders.AUTHORIZATION,
                        bearer(JwtTestDataFactory.createTamperedToken(memberId, UUID.randomUUID()))));
    }

    @Test
    void shouldAnswerCorsPreflightWithoutToken() throws Exception {
        mockMvc.perform(options(protectedUrl)
                        .header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN));

        verifyNoInteractions(boardService);
    }

    @Test
    void shouldNotRequireTokenForApiDocs() throws Exception {
        // springdoc is not part of the WebMvc slice, so a permitted path falls through to 404, not 401
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isNotFound());

        verifyNoInteractions(boardService);
    }

    // The rejected request must answer 401 with the plain-text message and never reach the service.
    private void assertRejected(MockHttpServletRequestBuilder request) throws Exception {
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists(HttpHeaders.WWW_AUTHENTICATE))
                .andExpect(content().string(UNAUTHENTICATED_BODY));

        verifyNoInteractions(boardService);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
