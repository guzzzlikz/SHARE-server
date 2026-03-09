package org.example.shareserver;

import org.example.shareserver.models.LoginDTO;
import org.example.shareserver.models.Product;
import org.example.shareserver.models.User;
import org.example.shareserver.services.JWTService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerSuccess() throws Exception {
        User user = User.builder()
                .id("228")
                .email("example228@gmail.com")
                .firstName("Anton")
                .lastName("Chigur")
                .password("pososi")
                .build();
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());
    }
    @Test
    void loginSuccess() throws Exception {
        LoginDTO loginDTO = LoginDTO.builder()
                .email("example228@gmail.com")
                .password("pososi")
                .build();
        mockMvc.perform(get("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk());
    }

}
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JWTService jwtService;

    @Test
    void addSuccess() throws Exception {
        Product product = Product.builder()
                .id("1")
                .title("testProduct")
                .description("testProductDesc")
                .price(123)
                .latitude(45.9204)
                .longitude(65.2049)
                .build();
        mockMvc.perform(post("/api/products/add")
                .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " +
                                jwtService.generateToken("example228@gmail.com"))
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk());
    }
}
