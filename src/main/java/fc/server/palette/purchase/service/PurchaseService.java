package fc.server.palette.purchase.service;

import fc.server.palette._common.exception.Exception404;
import fc.server.palette._common.exception.message.ExceptionMessage;
import fc.server.palette.member.entity.Member;
import fc.server.palette.purchase.dto.request.EditOfferDto;
import fc.server.palette.purchase.dto.response.MemberDto;
import fc.server.palette.purchase.dto.response.OfferDto;
import fc.server.palette.purchase.dto.response.OfferListDto;
import fc.server.palette.purchase.entity.Bookmark;
import fc.server.palette.purchase.entity.Media;
import fc.server.palette.purchase.entity.Purchase;
import fc.server.palette.purchase.entity.PurchaseParticipant;
import fc.server.palette.purchase.repository.PurchaseBookmarkRepository;
import fc.server.palette.purchase.repository.PurchaseMediaRepository;
import fc.server.palette.purchase.repository.PurchaseParticipantRepository;
import fc.server.palette.purchase.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseService {
    private final PurchaseRepository purchaseRepository;
    private final PurchaseMediaRepository purchaseMediaRepository;
    private final PurchaseBookmarkRepository purchaseBookmarkRepository;
    private final PurchaseParticipantRepository purchaseParticipantRepository;

    @Transactional(readOnly = true)
    public List<OfferListDto> getAllOffers() {
        List<Purchase> purchases = purchaseRepository.findAll();
        return purchases.stream()
                .map(this::buildOfferList)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OfferDto getOffer(Long offerId) {
        Purchase purchase = purchaseRepository.findById(offerId)
                .orElseThrow(() -> new Exception404(ExceptionMessage.OBJECT_NOT_FOUND + offerId));
        return buildOffer(purchase);
    }

    @Transactional(readOnly = true)
    public Purchase getPurchase(Long offerId) {
        return purchaseRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("공동구매 객체가 존재하지 않습니다."));
    }

    @Transactional
    public void saveImages(List<Media> mediaList) {
        purchaseMediaRepository.saveAll(mediaList);
    }

    @Transactional
    public void deleteImages(List<String> urls) {
        urls
                .forEach(url ->
                        purchaseMediaRepository.delete(
                                purchaseMediaRepository.findByUrl(url)
                        ));
    }

    @Transactional
    public OfferDto createOffer(Purchase purchase, List<Media> mediaList) {
        Purchase savedPurchase = purchaseRepository.save(purchase);
        purchaseMediaRepository.saveAll(mediaList);
        return buildOffer(savedPurchase);
    }

    private OfferDto buildOffer(Purchase purchase) {
        return OfferDto.builder()
                .id(purchase.getId())
                .member(MemberDto.of(purchase.getMember()))
                .title(purchase.getTitle())
                .category(purchase.getCategory())
                .startDate(purchase.getStartDate())
                .endDate(purchase.getEndDate())
                .price(purchase.getPrice())
                .description(purchase.getDescription())
                .shopUrl(purchase.getShopUrl())
                .headCount(purchase.getHeadCount())
                .bookmarkCount(getBookmarkCount(purchase.getId()))
                .image(getImagesUrl(purchase.getId()))
                .currentParticipantCount(getCurrentParticipants(purchase.getId()))
                .isClosing(purchase.getIsClosing())
                .hits(purchase.getHits())
                .created_at(purchase.getCreatedAt())
                .build();
    }

    private OfferListDto buildOfferList(Purchase purchase) {
        return OfferListDto.builder()
                .id(purchase.getId())
                .title(purchase.getTitle())
                .category(purchase.getCategory())
                .price(purchase.getPrice())
                .thumbnailUrl(getThumbnailUrl(purchase.getId()))
                .bookmarkCount(getBookmarkCount(purchase.getId()))
                .hits(purchase.getHits())
                .build();
    }

    private String getThumbnailUrl(Long purchaseId) {
        Optional<Media> optionalThumbnail = purchaseMediaRepository.findAllByPurchase_id(purchaseId)
                .stream()
                .findFirst();
        if (optionalThumbnail.isPresent()){
            return optionalThumbnail.get().getUrl();
        }
        return ExceptionMessage.OBJECT_NOT_FOUND;
    }

    private List<String> getImagesUrl(Long purchaseId) {
        return purchaseMediaRepository.findAllByPurchase_id(purchaseId)
                .stream()
                .map(Media::getUrl)
                .collect(Collectors.toList());
    }

    private Integer getBookmarkCount(Long purchaseId) {
        return purchaseBookmarkRepository.findAllByPurchase_id(purchaseId).size();
    }

    private Integer getCurrentParticipants(Long purchaseId) {
        List<PurchaseParticipant> allByPurchaseId = purchaseParticipantRepository.findAllByPurchaseId(purchaseId);
        return allByPurchaseId.size();
    }

    @Transactional
    public void addBookmark(Long offerId, Member member) {
        Purchase purchase = purchaseRepository.findById(offerId)
                .orElseThrow(() -> new Exception404(ExceptionMessage.OBJECT_NOT_FOUND));

        Bookmark bookmark = Bookmark.builder()
                .member(member)
                .purchase(purchase)
                .build();
        purchaseBookmarkRepository.save(bookmark);
    }

    @Transactional
    public void deleteOffer(Long offerId) {
        purchaseRepository.deleteById(offerId);
    }

    @Transactional
    public OfferDto editOffer(Long offerId, EditOfferDto editOfferDto) {
        Purchase purchase = purchaseRepository.findById(offerId)
                .orElseThrow(() -> new Exception404(ExceptionMessage.OBJECT_NOT_FOUND));
        purchase.updateOffer(editOfferDto);
        return buildOffer(purchase);
    }

    @Transactional
    public OfferDto closeOffer(Long offerId) {
        Purchase purchase = purchaseRepository.findById(offerId)
                .orElseThrow(() -> new Exception404(ExceptionMessage.OBJECT_NOT_FOUND));
        purchase.closeOffer();
        return buildOffer(purchase);
    }

    @Transactional(readOnly = true)
    public Long getAuthorId(Long offerId) {
        Purchase purchase = purchaseRepository.findById(offerId)
                .orElseThrow(() -> new Exception404(ExceptionMessage.OBJECT_NOT_FOUND));
        return purchase.getMember().getId();
    }
}
