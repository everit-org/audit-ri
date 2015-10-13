package org.everit.audit.ri;

import javax.annotation.Generated;

/**
 * The cache key of a cached event type.
 */
public class CachedEventTypeKey {

  public final long applicationId;

  public final String eventTypeName;

  public CachedEventTypeKey(final long applicationId, final String eventTypeName) {
    this.applicationId = applicationId;
    this.eventTypeName = eventTypeName;
  }

  @Override
  @Generated("eclipse")
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CachedEventTypeKey other = (CachedEventTypeKey) obj;
    if (applicationId != other.applicationId) {
      return false;
    }
    if (eventTypeName == null) {
      if (other.eventTypeName != null) {
        return false;
      }
    } else if (!eventTypeName.equals(other.eventTypeName)) {
      return false;
    }
    return true;
  }

  @Override
  @Generated("eclipse")
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + (int) (applicationId ^ (applicationId >>> 32));
    result = (prime * result) + ((eventTypeName == null) ? 0 : eventTypeName.hashCode());
    return result;
  }

}
