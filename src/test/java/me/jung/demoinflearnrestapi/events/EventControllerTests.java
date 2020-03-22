package me.jung.demoinflearnrestapi.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.jung.demoinflearnrestapi.common.TestDescription;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDateTime;

import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class EventControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

//    @MockBean
//    EventRepository eventRepository;


    @Autowired
    private WebApplicationContext ctx;
    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))  // 필터 추가
                .alwaysDo(print())
                .build();
    }


    @Test
    @TestDescription("정상적으로 이벤트를 생성하는 테스트")
    public void createEvent() throws Exception {
        EventDto event = EventDto.builder()
                .name("Spring")
                .description("Rest API development with Srpung")
                .beginEnrollmentDateTime(LocalDateTime.of(2020,3,22,14,38,0))
                .closeEnrollmentDateTime(LocalDateTime.of(2020,3,23,14,38,0))
                .beginEventDateTime(LocalDateTime.of(2020,3,24,14,38,0))
                .endEventDateTime(LocalDateTime.of(2020,3,25,14,38,0))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스사텁팩토리")
                .build();
//        event.setId(10);
//        Mockito.when(eventRepository.save(event)).thenReturn(event);

        mockMvc.perform(post("/api/events/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(HAL_JSON)
                .content(objectMapper.writeValueAsString(event))
                .characterEncoding("utf-8")
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("id").value(Matchers.not(100)))
                .andExpect(jsonPath("free").value(false))
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
                .andExpect(header().exists(HttpHeaders.LOCATION));
//                .andExpect(header().(HttpHeaders.CONTENT_TYPE, HAL_JSON.toString()));
    }
    @Test
    @TestDescription("입력 받을 수 없는 값을 사용한 경우에 에러가 발생하는 테스트")
    public void createEvent_badRequest() throws Exception {
        Event event = Event.builder()
                .id(100)// X
                .name("Spring")
                .description("Rest API development with Srpung")
                .beginEnrollmentDateTime(LocalDateTime.of(2020,3,22,14,38,0))
                .closeEnrollmentDateTime(LocalDateTime.of(2020,3,23,14,38,0))
                .beginEventDateTime(LocalDateTime.of(2020,3,24,14,38,0))
                .endEventDateTime(LocalDateTime.of(2020,3,25,14,38,0))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스사텁팩토리")
                .free(true)// X
                .offline(false)// X
                .eventStatus(EventStatus.PUBLISHED)// X
                .build();
        mockMvc.perform(post("/api/events/")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(HAL_JSON)
                .content(objectMapper.writeValueAsString(event))
                .characterEncoding("utf-8")
        )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("입력 값이 비어있는 경우에 에러가 발생")
    public void createEvent_Bad_Request_Empty_Input () throws Exception {
        EventDto  eventDto = EventDto.builder().build();
        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventDto))
        )
                .andExpect(status().isBadRequest());
    }
    @Test
    @TestDescription("입력 값이 잘못된 경우에 에러가 발생")
    public void createEvent_Bad_Request_Wrong_Input () throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("Rest API development with Srpung")
                .beginEnrollmentDateTime(LocalDateTime.of(2020,3,24,14,38,0))
                .closeEnrollmentDateTime(LocalDateTime.of(2020,3,22,14,38,0))
                .beginEventDateTime(LocalDateTime.of(2020,3,26,14,38,0))
                .endEventDateTime(LocalDateTime.of(2020,3,24,14,38,0))
                .basePrice(200)
                .maxPrice(100)
                .limitOfEnrollment(100)
                .location("강남역 D2 스사텁팩토리")
                .build();
        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventDto))
        )

                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
//                .andExpect(jsonPath("$[0].field").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].code").exists())
//                .andExpect(jsonPath("$[0].rejectedValue").exists())
        ;
    }



}
