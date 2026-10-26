package com.vrund.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:tasks;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "app.bootstrap.username=vrund", "app.bootstrap.password=test-password-12345"
})
@AutoConfigureMockMvc
class TaskApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    long create(String owner, String title) throws Exception {
        var response = mvc.perform(post("/api/tasks").with(user(owner)).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(json.writeValueAsString(java.util.Map.of("title", title, "description", "Details", "completed", false))))
            .andExpect(status().isCreated()).andExpect(header().exists("Location"))
            .andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("id").asLong();
    }

    @Test void rejectsAnonymousApiRequests() throws Exception {
        mvc.perform(get("/api/tasks")).andExpect(status().isUnauthorized());
    }
    @Test void loginWorksWithRealPassword() throws Exception {
        mvc.perform(formLogin().user("vrund").password("test-password-12345"))
            .andExpect(authenticated().withUsername("vrund"));
    }
    @Test void wrongPasswordIsRejected() throws Exception {
        mvc.perform(formLogin().user("vrund").password("wrong"))
            .andExpect(unauthenticated());
    }
    @Test void createsReadsUpdatesAndDeletesTask() throws Exception {
        long id = create("lifecycle", "Learn Spring Boot");
        mvc.perform(get("/api/tasks/" + id).with(user("lifecycle")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Learn Spring Boot"));
        mvc.perform(put("/api/tasks/" + id).with(user("lifecycle")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\":\"Finished learning\",\"description\":\"Done\",\"completed\":true}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.completed").value(true));
        mvc.perform(delete("/api/tasks/" + id).with(user("lifecycle")).with(csrf()))
            .andExpect(status().isNoContent());
        mvc.perform(get("/api/tasks/" + id).with(user("lifecycle"))).andExpect(status().isNotFound());
    }
    @Test void anotherUserCannotReadUpdateOrDeleteTask() throws Exception {
        long id = create("owner", "Private task");
        mvc.perform(get("/api/tasks/" + id).with(user("other"))).andExpect(status().isNotFound());
        mvc.perform(put("/api/tasks/" + id).with(user("other")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\":\"Stolen\",\"completed\":true}"))
            .andExpect(status().isNotFound());
        mvc.perform(delete("/api/tasks/" + id).with(user("other")).with(csrf())).andExpect(status().isNotFound());
        mvc.perform(get("/api/tasks/" + id).with(user("owner")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Private task"));
    }
    @Test void listOnlyShowsCurrentUsersTasks() throws Exception {
        create("list-owner", "Visible"); create("list-other", "Hidden");
        mvc.perform(get("/api/tasks").with(user("list-owner")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(1))
            .andExpect(jsonPath("$.items[0].title").value("Visible"));
    }
    @Test void requiresCsrfForWrites() throws Exception {
        mvc.perform(post("/api/tasks").with(user("vrund"))
            .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Test\",\"completed\":false}"))
            .andExpect(status().isForbidden());
    }
    @Test void rejectsBlankAndOversizedTitles() throws Exception {
        for (String title : new String[]{"   ", "x".repeat(141)}) {
            mvc.perform(post("/api/tasks").with(user("vrund")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(java.util.Map.of("title", title, "completed", false))))
                .andExpect(status().isBadRequest());
        }
    }
    @Test void rejectsMissingCompletedFlagAndMalformedJson() throws Exception {
        for (String body : new String[]{"{\"title\":\"Test\"}", "{broken"}) {
            mvc.perform(post("/api/tasks").with(user("vrund")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());
        }
    }
    @Test void boundsPagination() throws Exception {
        mvc.perform(get("/api/tasks?size=101").with(user("vrund"))).andExpect(status().isBadRequest());
        mvc.perform(get("/api/tasks?page=-1").with(user("vrund"))).andExpect(status().isBadRequest());
    }
    @Test void pageRendersCsrfAndUserWithoutLeakingCredentials() throws Exception {
        var html = mvc.perform(get("/").with(user("vrund"))).andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
            .andReturn().getResponse().getContentAsString();
        assertThat(html).contains("csrf-token", "vrund").doesNotContain("test-password-12345");
    }
    @Test void logoutInvalidatesSession() throws Exception {
        mvc.perform(post("/logout").with(user("vrund")).with(csrf())).andExpect(unauthenticated());
    }
}
