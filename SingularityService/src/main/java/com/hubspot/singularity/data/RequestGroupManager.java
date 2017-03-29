package com.hubspot.singularity.data;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubspot.singularity.SingularityCreateResult;
import com.hubspot.singularity.SingularityDeleteResult;
import com.hubspot.singularity.SingularityRequestGroup;
import com.hubspot.singularity.config.SingularityConfiguration;
import com.hubspot.singularity.data.transcoders.Transcoder;

@Singleton
public class RequestGroupManager extends CuratorAsyncManager {
  private static final String REQUEST_GROUP_ROOT = "/groups";
  private static final String ALL_GROUPS_KEY = "all";

  private final Transcoder<SingularityRequestGroup> requestGroupTranscoder;
  private final Cache<String, List<SingularityRequestGroup>> requestGroupCache;

  @Inject
  public RequestGroupManager(CuratorFramework curator, SingularityConfiguration configuration,
                             MetricRegistry metricRegistry, Transcoder<SingularityRequestGroup> requestGroupTranscoder) {
    super(curator, configuration, metricRegistry);
    this.requestGroupTranscoder = requestGroupTranscoder;
    this.requestGroupCache = CacheBuilder.newBuilder()
        .expireAfterAccess(configuration.getCacheUiDataForMs(), TimeUnit.MILLISECONDS)
        .build();
  }

  private String getRequestGroupPath(String requestGroupId) {
    return ZKPaths.makePath(REQUEST_GROUP_ROOT, requestGroupId);
  }

  public List<String> getRequestGroupIds() {
    return getChildren(REQUEST_GROUP_ROOT);
  }

  public List<SingularityRequestGroup> getCachedRequestGroups() {
    List<SingularityRequestGroup> maybeGroups = requestGroupCache.getIfPresent(ALL_GROUPS_KEY);
    if (maybeGroups != null) {
      return maybeGroups;
    } else {
      maybeGroups = getRequestGroups();
      requestGroupCache.put(ALL_GROUPS_KEY, maybeGroups);
      return maybeGroups;
    }
  }

  public List<SingularityRequestGroup> getRequestGroups() {
    return getAsyncChildren(REQUEST_GROUP_ROOT, requestGroupTranscoder);
  }

  public Optional<SingularityRequestGroup> getRequestGroup(String requestGroupId) {
    return getData(getRequestGroupPath(requestGroupId), requestGroupTranscoder);
  }

  public SingularityCreateResult saveRequestGroup(SingularityRequestGroup requestGroup) {
    return save(getRequestGroupPath(requestGroup.getId()), requestGroup, requestGroupTranscoder);
  }

  public SingularityDeleteResult deleteRequestGroup(String requestGroupId) {
    return delete(getRequestGroupPath(requestGroupId));
  }
}
