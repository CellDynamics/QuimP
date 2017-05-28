package com.github.celldynamics.quimp;

/**
 * Holds version and build information.
 * 
 * @author p.baniukiewicz
 *
 */
public class QuimpVersion {

  // This class is used in Serializer, so changing naming may affect serialization and
  // deserialization.
  private String version;
  private String buildstamp;
  private String name;

  /**
   * Default constructor. Set everything to zero.
   */
  public QuimpVersion() {
    this.version = "0.0.0";
    this.buildstamp = "0.0.0";
    this.name = "0.0.0";
  }

  /**
   * Set version data.
   * 
   * @param version version like 1.2.3
   * @param buildstamp Date and time
   * @param name Software name (QuimP)
   */
  public QuimpVersion(String version, String buildstamp, String name) {
    super();
    this.version = version;
    this.buildstamp = buildstamp;
    this.name = name;
  }

  /**
   * Get Version.
   * 
   * @return the version
   */
  public String getVersion() {
    return version;
  }

  /**
   * Get time and date.
   * 
   * @return the buildstamp
   */
  public String getBuildstamp() {
    return buildstamp;
  }

  /**
   * Get software name.
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "QuimpVersion [version=" + version + ", buildstamp=" + buildstamp + ", name=" + name
            + "]";
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((buildstamp == null) ? 0 : buildstamp.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    QuimpVersion other = (QuimpVersion) obj;
    if (buildstamp == null) {
      if (other.buildstamp != null) {
        return false;
      }
    } else if (!buildstamp.equals(other.buildstamp)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (version == null) {
      if (other.version != null) {
        return false;
      }
    } else if (!version.equals(other.version)) {
      return false;
    }
    return true;
  }

}
