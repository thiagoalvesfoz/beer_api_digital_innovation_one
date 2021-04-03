package one.digitalinnovation.beerstock.controller;

import one.digitalinnovation.beerstock.builder.BeerDTOBuilder;
import one.digitalinnovation.beerstock.dto.BeerDTO;
import one.digitalinnovation.beerstock.exception.BeerNotFoundException;
import one.digitalinnovation.beerstock.service.BeerService;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import static java.lang.String.format;
import static one.digitalinnovation.beerstock.utils.JsonConvertionUtils.asJsonString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(MockitoExtension.class)
public class BeerControllerTest {

    private static final String BEER_API_URL_PATH = "/api/v1/beers";
    private static final long VALID_BEER_ID = 1L;
    private static final long INVALID_BEER_ID = 2L;
    private static final String BEER_API_SUB_PATH_INCREMENT_URL = "/increment";
    private static final String BEER_API_SUB_PATH_DECREMENT_URL = "/decrement";
    MockMvc mockMvc;
    @Mock
    private BeerService beerService;
    @InjectMocks
    private BeerController beerController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(beerController).
                setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver()). // adiciona suporte a objetos pagináveis
                setViewResolvers((s, locale) -> new MappingJackson2JsonView()).build(); // mapeia o retorno para JSON
    }

    @Test
    void whenPOSTIsCalledThenABeerIsCreated() throws Exception {
        // GIVEN
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        // WHEN
        when(beerService.createBeer(beerDTO)).thenReturn(beerDTO);

        mockMvc.perform(post(BEER_API_URL_PATH).
                contentType(MediaType.APPLICATION_JSON).
                content(asJsonString(beerDTO))).
                andExpect(status().isCreated()).
                andExpect(jsonPath("$.name", Is.is(beerDTO.getName()))).
                andExpect(jsonPath("$.brand", Is.is(beerDTO.getBrand()))).
                andExpect(jsonPath("$.type", Is.is(beerDTO.getType().toString())));
    }

    @Test
    void whenPOSTIsCalledWithoutRequiredFieldThenAnErrorIsReturned() throws Exception {
        // GIVEN
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        beerDTO.setBrand(null);

        mockMvc.perform(post(BEER_API_URL_PATH).
                contentType(MediaType.APPLICATION_JSON).
                content(asJsonString(beerDTO))).
                andExpect(status().isBadRequest());
    }

    @Test
    void whenGETIsCalledWithValidNameThenOkStatusIsReturned() throws Exception {
        // GIVEN
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        // WHEN
        when(beerService.findByName(beerDTO.getName())).thenReturn(beerDTO);

        mockMvc.perform(get(BEER_API_URL_PATH.concat(format("/%s", beerDTO.getName()))).
                contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andExpect(jsonPath("$.name", Is.is(beerDTO.getName()))).
                andExpect(jsonPath("$.brand", Is.is(beerDTO.getBrand()))).
                andExpect(jsonPath("$.type", Is.is(beerDTO.getType().toString())));
    }

    @Test
    void whenGETIsCalledWithoutRegisteredNameThenNotFoundIsReturned() throws Exception {
        // GIVEN
        BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        // WHEN
        when(beerService.findByName(beerDTO.getName())).thenThrow(BeerNotFoundException.class);

        mockMvc.perform(get(BEER_API_URL_PATH.concat(format("/%s", beerDTO.getName()))).
                contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isNotFound());
    }
}