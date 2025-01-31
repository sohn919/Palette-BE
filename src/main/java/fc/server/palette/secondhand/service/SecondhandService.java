package fc.server.palette.secondhand.service;

import fc.server.palette._common.exception.Exception404;
import fc.server.palette._common.exception.message.ExceptionMessage;
import fc.server.palette.purchase.dto.response.MemberDto;
import fc.server.palette.secondhand.dto.request.EditProductDto;
import fc.server.palette.secondhand.dto.response.ProductDto;
import fc.server.palette.secondhand.dto.response.ProductListDto;
import fc.server.palette.secondhand.entity.Media;
import fc.server.palette.secondhand.entity.Secondhand;
import fc.server.palette.secondhand.repository.SecondhandBookmarkRepository;
import fc.server.palette.secondhand.repository.SecondhandMediaRepository;
import fc.server.palette.secondhand.repository.SecondhandRespository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SecondhandService {
    private final SecondhandRespository secondhandRespository;
    private final SecondhandMediaRepository secondhandMediaRepository;
    private final SecondhandBookmarkRepository secondhandBookmarkRepository;

    @Transactional(readOnly = true)
    public List<ProductListDto> getAllProducts() {
        List<Secondhand> products = secondhandRespository.findAll();
        return products.stream()
                .map(this::buildProductList)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductDto getProduct(Long productId) {
        Secondhand product = secondhandRespository.findById(productId)
                .orElseThrow(() -> new Exception404(ExceptionMessage.OBJECT_NOT_FOUND));
        return buildProductDto(product);
    }

    @Transactional(readOnly = true)
    public Secondhand getSecondhand(Long productId) {
        return secondhandRespository.findById(productId)
                .orElseThrow(() -> new Exception404(ExceptionMessage.OBJECT_NOT_FOUND));
    }

    @Transactional
    public void saveImages(List<Media> mediaList) {
        secondhandMediaRepository.saveAll(mediaList);
    }

    @Transactional
    public void deleteImages(List<String> urls) {
        urls
                .forEach(url ->
                        secondhandMediaRepository.delete(
                                secondhandMediaRepository.findByUrl(url)
                        ));
    }

    @Transactional
    public void deleteProduct(Long productId) {
        secondhandRespository.deleteById(productId);
    }

    @Transactional
    public ProductDto closeTransaction(Long productId) {
        Secondhand product = secondhandRespository.findById(productId)
                .orElseThrow(() -> new Exception404(ExceptionMessage.OBJECT_NOT_FOUND));
        product.closeTransaction();
        return buildProductDto(product);
    }

    @Transactional
    public ProductDto createProduct(Secondhand secondhand, List<Media> mediaList) {
        Secondhand savedProduct = secondhandRespository.save(secondhand);
        secondhandMediaRepository.saveAll(mediaList);
        return buildProductDto(savedProduct);
    }

    @Transactional
    public ProductDto editProduct(Long productId, EditProductDto editProductDto) {
        Secondhand product = secondhandRespository.findById(productId)
                .orElseThrow(() -> new Exception404(ExceptionMessage.OBJECT_NOT_FOUND));
        product.updateProduct(editProductDto);
        return buildProductDto(product);
    }

    private ProductListDto buildProductList(Secondhand secondhand) {
        return ProductListDto.builder()
                .id(secondhand.getId())
                .title(secondhand.getTitle())
                .category(secondhand.getCategory())
                .price(secondhand.getPrice())
                .thumbnailUrl(getThumbnailUrl(secondhand.getId()))
                .bookmarkCount(getBookmarkCount(secondhand.getId()))
                .hits(secondhand.getHits())
                .build();
    }

    private ProductDto buildProductDto(Secondhand secondhand) {
        return ProductDto.builder()
                .id(secondhand.getId())
                .member(MemberDto.of(secondhand.getMember()))
                .bookmarkCount(getBookmarkCount(secondhand.getId()))
                .images(getImagesUrl(secondhand.getId()))
                .title(secondhand.getTitle())
                .category(secondhand.getCategory())
                .transactionStartTime(secondhand.getTransactionStartTime())
                .transactionEndTime(secondhand.getTransactionEndTime())
                .description(secondhand.getDescription())
                .price(secondhand.getPrice())
                .hits(secondhand.getHits())
                .isSoldOut(secondhand.getIsSoldOut())
                .isFree(secondhand.getIsFree())
                .createdAt(secondhand.getCreatedAt())
                .build();
    }

    private String getThumbnailUrl(Long secondhandId) {
        Optional<Media> optionalThumbnail = secondhandMediaRepository.findAllBySecondhand_id(secondhandId)
                .stream()
                .findFirst();
        if (optionalThumbnail.isPresent()){
            return optionalThumbnail.get().getUrl();
        }
        return ExceptionMessage.OBJECT_NOT_FOUND;
    }

    @Transactional(readOnly = true)
    public Long getAuthorId(Long productId) {
        Secondhand product = secondhandRespository.findById(productId)
                .orElseThrow(() -> new Exception404(ExceptionMessage.OBJECT_NOT_FOUND));
        return product.getMember().getId();
    }

    private List<String> getImagesUrl(Long secondhandId) {
        return secondhandMediaRepository.findAllBySecondhand_id(secondhandId)
                .stream()
                .map(Media::getUrl)
                .collect(Collectors.toList());
    }

    private Integer getBookmarkCount(Long secondhandId) {
        return secondhandBookmarkRepository.findAllBySecondhand_id(secondhandId).size();
    }
}
