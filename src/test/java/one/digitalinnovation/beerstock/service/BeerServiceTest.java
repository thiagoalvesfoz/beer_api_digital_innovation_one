package one.digitalinnovation.beerstock.service;

import one.digitalinnovation.beerstock.builder.BeerDTOBuilder;
import one.digitalinnovation.beerstock.dto.BeerDTO;
import one.digitalinnovation.beerstock.entity.Beer;
import one.digitalinnovation.beerstock.exception.BeerAlreadyRegisteredException;
import one.digitalinnovation.beerstock.exception.BeerNotFoundException;
import one.digitalinnovation.beerstock.mapper.BeerMapper;
import one.digitalinnovation.beerstock.repository.BeerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Teste Unitário em BeerService")
public class BeerServiceTest {

    private static final long INVALID_BEER_ID = 1L;

    private final BeerMapper beerMapper = BeerMapper.INSTANCE;

    @Mock
    private BeerRepository beerRepository;

    @InjectMocks
    private BeerService beerService;

    @Test
    @DisplayName("Quando informado a cerveja então deverá ser criado")
    void whenBeerInformedThenItShouldBeCreated() throws BeerAlreadyRegisteredException {
        // GIVEN
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer beer = beerMapper.toModel(expectedBeerDTO);

        // WHEN
        when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.empty());
        when(beerRepository.save(beer)).thenReturn(beer);

        //THEN
        BeerDTO createdBearDTO = beerService.createBeer(expectedBeerDTO);

        //ASSERT WITH HAMCREST MATCHERS
        assertThat(createdBearDTO.getId(), is(equalTo(expectedBeerDTO.getId())));
        assertThat(createdBearDTO.getName(), is(equalTo(expectedBeerDTO.getName())));
        assertThat(createdBearDTO.getQuantity(), is(equalTo(expectedBeerDTO.getQuantity())));
        assertThat(createdBearDTO.getQuantity(), is(greaterThan(2)));
    }

    @Test
    @DisplayName("Quando a cerveja informada já estiver cadastrada deve-se lançar uma exceção")
    void whenTheBeerInformedIsAlreadyRegisteredThenAnExceptionMustBeThrow() {
        // GIVEN
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer duplicatedBeer = beerMapper.toModel(expectedBeerDTO);

        // WHEN
        when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.of(duplicatedBeer));

        // THEN ASSERT THROW
        assertThrows(BeerAlreadyRegisteredException.class, () -> beerService.createBeer(expectedBeerDTO));
    }

    @Test
    @DisplayName("Quando um nome de cerveja válido é fornecido, então retorne uma cerveja")
    void whenValidBeerNameIsGivenThenReturnABeer() throws BeerNotFoundException {
        // GIVEN
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedFoundBear = beerMapper.toModel(expectedBeerDTO);

        // WHEN
        when(beerRepository.findByName(expectedFoundBear.getName())).thenReturn(Optional.of(expectedFoundBear));

        // THEN
        BeerDTO foundBearDTO = beerService.findByName(expectedBeerDTO.getName());

        //ASSERT WITH HAMCREST MATCHERS
        assertThat(foundBearDTO, is(equalTo(expectedBeerDTO)));
    }

    @Test
    @DisplayName("Quando o nome da cerveja não registrada é fornecido, lance uma exceção")
    void whenNotRegisteredBeerNameIsGivenThenThrowAnException() {
        // GIVEN
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        // WHEN
        when(beerRepository.findByName(expectedBeerDTO.getName())).thenReturn(Optional.empty());

        // THEN ASSERT THROW
        assertThrows(BeerNotFoundException.class, () -> beerService.findByName(expectedBeerDTO.getName()));
    }

    @Test
    @DisplayName("Quando a listagem de cervejas for chamada, então retorne a lista de cervejas")
    void whenListBeerIsCalledThenReturnListOfBeers() {
        // GIVEN
        BeerDTO expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedFoundBear = beerMapper.toModel(expectedBeerDTO);

        // WHEN
        when(beerRepository.findAll()).thenReturn(Collections.singletonList(expectedFoundBear));
        
        // THEN
        List<BeerDTO> foundListBeerDTOS = beerService.listAll();

        //ASSERT WITH HAMCREST MATCHERS
        assertThat(foundListBeerDTOS, is(not(empty())));
        assertThat(foundListBeerDTOS, contains(expectedBeerDTO));
    }

    @Test
    @DisplayName("Quando a listagem de cerveja é chamada, então retorna uma lista vazia de cervejas")
    void whenListBeerIsCalledThenReturnAnEmptyListOfBeers() {
        // WHEN
        when(beerRepository.findAll()).thenReturn(Collections.emptyList());

        // THEN
        List<BeerDTO> foundListBeerDTOS = beerService.listAll();

        //ASSERT WITH HAMCREST MATCHERS
        assertThat(foundListBeerDTOS, is(empty()));
    }

    @Test
    @DisplayName("Quando a exclusão é chamada com um ID válido, então a cerveja deve ser excluida")
    void whenExclusionIsCalledWithValidIdThenABeerShouldBeDeleted() throws BeerNotFoundException {
        // GIVEN
        BeerDTO expectedDeletedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedDeletedBeer = beerMapper.toModel(expectedDeletedBeerDTO);

        // WHEN
        when(beerRepository.findById(expectedDeletedBeer.getId())).thenReturn(Optional.of(expectedDeletedBeer));
        doNothing().when(beerRepository).deleteById(expectedDeletedBeer.getId());

        // THEN
        beerService.deleteById(expectedDeletedBeerDTO.getId());

        //ASSERT WITH VERIFY
        verify(beerRepository, times(1)).findById(expectedDeletedBeerDTO.getId());
        verify(beerRepository, times(1)).deleteById(expectedDeletedBeerDTO.getId());
    }

    @Test
    @DisplayName("Quando a exclusão é chamada com um ID invalido, então uma exceção deve ser lançada")
    void whenExclusionIsCalledWithInvalidIdThenThrowAnException() throws BeerNotFoundException {
        // GIVEN
        BeerDTO deleteInvalidBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

        // WHEN
        when(beerRepository.findById(deleteInvalidBeerDTO.getId())).thenReturn(Optional.empty());

        // THEN ASSERT THROW
        assertThrows(
                BeerNotFoundException.class,
                () ->  beerService.deleteById(deleteInvalidBeerDTO.getId())
        );
    }

}
