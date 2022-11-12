package com.github.tedemorgado.koerber.service;

import com.github.tedemorgado.koerber.controller.model.CreateFilter;
import com.github.tedemorgado.koerber.controller.model.Filter;
import com.github.tedemorgado.koerber.exception.BadRequestException;
import com.github.tedemorgado.koerber.exception.EntityNotFoundException;
import com.github.tedemorgado.koerber.persistence.model.FilterEntity;
import com.github.tedemorgado.koerber.persistence.model.ScreenEntity;
import com.github.tedemorgado.koerber.persistence.model.UserEntity;
import com.github.tedemorgado.koerber.persistence.repository.FilterRepository;
import com.github.tedemorgado.koerber.persistence.repository.ScreenRepository;
import com.github.tedemorgado.koerber.persistence.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FilterService {

   private final FilterRepository filterRepository;
   private final UserRepository userRepository;
   private final ScreenRepository screenRepository;

   public FilterService(final FilterRepository filterRepository, final UserRepository userRepository, final ScreenRepository screenRepository) {
      this.filterRepository = filterRepository;
      this.userRepository = userRepository;
      this.screenRepository = screenRepository;
   }

   private static void mapFilterEntityWithCreateFilter(final CreateFilter createFilter, final UserEntity userEntity, final ScreenEntity screenEntity, final FilterEntity filterEntity) {
      filterEntity.setUser(userEntity);
      filterEntity.setScreen(screenEntity);
      filterEntity.setName(createFilter.getName());
      filterEntity.setData(createFilter.getData());
      filterEntity.setOutputFilter(createFilter.getOutputFilter());
   }

   /*
   • Update filter
   • Soft delete filter
   • List all filter (latest version) (Optional)
    */
   @Transactional
   public Filter updateFilter(final UUID filterId, final Filter filter) {
      if (!filterId.equals(filter.getId())) {
         throw new BadRequestException("Specified path filterId need to be the same as the object.");
      }

      final FilterEntity filterEntity = this.filterRepository.findAllByUuidOrderByVersionDesc(filterId)
         .stream()
         .findFirst()
         .orElseThrow(() -> new EntityNotFoundException("Filter not found for id " + filter.getId()));

      mapFilterEntityWithCreateFilter(filter, filterEntity.getUser(), filterEntity.getScreen(), filterEntity);
      filterEntity.setOutputFilter(filter.getOutputFilter());
      filterEntity.setVersion(filterEntity.getVersion() + 1);

      return this.mapFilterEntityToFilter(this.filterRepository.save(filterEntity));
   }

   @Transactional
   public Filter createFilter(final CreateFilter createFilter) {
      final UserEntity userEntity = this.getUserEntity(createFilter.getUserId());
      final ScreenEntity screenEntity = this.getScreenEntity(createFilter.getScreenId());

      final FilterEntity filterEntity = new FilterEntity();
      mapFilterEntityWithCreateFilter(createFilter, userEntity, screenEntity, filterEntity);
      filterEntity.setUuid(UUID.randomUUID());
      filterEntity.setVersion(1L);

      return this.mapFilterEntityToFilter(this.filterRepository.save(filterEntity));
   }

   @Transactional(readOnly = true)
   public Page<Filter> getAllFilters(final Pageable pageable) {
      return null;
      /*return this.filterRepository
         .distinct(pageable)
         .map(this::mapFilterEntityToFilter);*/
   }

   @Transactional(readOnly = true)
   public Set<Long> getFilterVersions(final UUID uuid) {
      final Set<Long> versions = this.filterRepository.findAllByUuidOrderByVersionDesc(uuid)
         .stream()
         .map(FilterEntity::getVersion)
         .sorted(Comparator.reverseOrder())
         .collect(Collectors.toCollection(LinkedHashSet::new));

      if (versions.isEmpty()) {
         throw new EntityNotFoundException("No filter found with id " + uuid);
      }

      return versions;
   }

   @Transactional(readOnly = true)
   public Filter getFilterById(final UUID uuid, final Long version) {
      final List<FilterEntity> filterEntities = this.filterRepository.findAllByUuidOrderByVersionDesc(uuid);

      final Optional<FilterEntity> filterEntity;
      if (version != null) {
         filterEntity = filterEntities.stream().filter(fe -> fe.getVersion().equals(version)).findFirst();
      } else {
         filterEntity = filterEntities
            .stream()
            .findFirst();
      }

      return filterEntity
         .map(this::mapFilterEntityToFilter)
         .orElseThrow(() -> new EntityNotFoundException("No filter found with id " + uuid));
   }

   private Filter mapFilterEntityToFilter(final FilterEntity filterEntity) {
      final UUID screenId = Optional.ofNullable(filterEntity.getScreen())
         .map(ScreenEntity::getUuid)
         .orElse(null);

      return new Filter(filterEntity.getUuid(), filterEntity.getUser().getUuid(), filterEntity.getName(), filterEntity.getData(), filterEntity.getOutputFilter(), screenId, filterEntity.getVersion());
   }

   private UserEntity getUserEntity(final UUID filter) {
      return this.userRepository.findByUuid(filter).orElseThrow(() -> new EntityNotFoundException("User not found for id " + filter));
   }

   private ScreenEntity getScreenEntity(final UUID filter) {
      if (filter != null) {
         return this.screenRepository.findByUuid(filter).orElseThrow(() -> new EntityNotFoundException("Screen not found for id " + filter));
      }
      return null;
   }
}
