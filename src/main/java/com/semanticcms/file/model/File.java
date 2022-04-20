/*
 * semanticcms-file-model - Files nested within SemanticCMS pages and elements.
 * Copyright (C) 2013, 2014, 2015, 2016, 2017, 2019, 2020, 2021, 2022  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of semanticcms-file-model.
 *
 * semanticcms-file-model is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * semanticcms-file-model is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with semanticcms-file-model.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.semanticcms.file.model;

import com.aoapps.hodgepodge.util.Tuple2;
import com.aoapps.lang.Strings;
import com.aoapps.net.Path;
import com.semanticcms.core.model.Element;
import com.semanticcms.core.model.ResourceRef;
import com.semanticcms.core.resources.ResourceConnection;
import com.semanticcms.core.resources.ResourceStore;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;

public class File extends Element {

  // resourceStore and resourceRef are only updated while holding the lock
  private volatile ResourceStore resourceStore;
  private volatile ResourceRef resourceRef;

  private volatile boolean hidden;

  /**
   * Does not include the size on the ID template, also strips any file extension if it will not leave the filename empty.
   */
  @Override
  protected String getElementIdTemplate() {
    ResourceRef rr = getResourceRef();
    if (rr != null) {
      String path = rr.getPath().toString();
      int slashBefore;
      if (path.endsWith(Path.SEPARATOR_STRING)) {
        slashBefore = path.lastIndexOf(Path.SEPARATOR_CHAR, path.length() - 2);
      } else {
        slashBefore = path.lastIndexOf(Path.SEPARATOR_CHAR);
      }
      String filename = path.substring(slashBefore + 1);
      if (filename.isEmpty()) {
        throw new IllegalArgumentException("Invalid filename for file: " + path);
      }
      // Strip extension if will not leave empty
      int lastDot = filename.lastIndexOf('.');
      if (lastDot > 0) {
        filename = filename.substring(0, lastDot);
      }
      return filename;
    }
    throw new IllegalStateException("Path not set");
  }

  /**
   * The label is always the filename.
   */
  @Override
  public String getLabel() {
    ResourceStore rs;
    ResourceRef rr;
    synchronized (lock) {
      rs = this.resourceStore;
      rr = this.resourceRef;
    }
    if (rr != null) {
      String path = rr.getPath().toString();
      boolean isDirectory = path.endsWith(Path.SEPARATOR_STRING);
      int slashBefore;
      if (isDirectory) {
        slashBefore = path.lastIndexOf(Path.SEPARATOR_CHAR, path.length() - 2);
      } else {
        slashBefore = path.lastIndexOf(Path.SEPARATOR_CHAR);
      }
      String filename = path.substring(slashBefore + 1);
      if (filename.isEmpty()) {
        throw new IllegalArgumentException("Invalid filename for file: " + path);
      }
      if (!isDirectory) {
        if (rs != null) {
          try {
            try (ResourceConnection conn = rs.getResource(rr.getPath()).open()) {
              if (conn.exists()) {
                return
                  filename
                  + " ("
                  + Strings.getApproximateSize(conn.getLength())
                  + ')'
                ;
              }
            }
          } catch (FileNotFoundException e) {
            // Resource removed between calls to exists() and getLength()
            // fall-through to return filename
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        }
      }
      return filename;
    }
    throw new IllegalStateException("Path not set");
  }

  public ResourceStore getResourceStore() {
    return resourceStore;
  }

  public ResourceRef getResourceRef() {
    return resourceRef;
  }

  public Tuple2<ResourceStore, ResourceRef> getResource() {
    synchronized (lock) {
      if (resourceStore == null && resourceRef == null) {
        return null;
      } else {
        assert resourceRef != null;
        return new Tuple2<>(resourceStore, resourceRef);
      }
    }
  }

  public void setResource(ResourceStore resourceStore, ResourceRef resourceRef) {
    if (resourceStore != null && resourceRef == null) {
      throw new IllegalArgumentException("resourceRef required when resourceStore provided");
    }
    synchronized (lock) {
      checkNotFrozen();
      this.resourceStore = resourceStore;
      this.resourceRef = resourceRef;
    }
  }

  @Override
  public boolean isHidden() {
    return hidden;
  }

  public void setHidden(boolean hidden) {
    checkNotFrozen();
    this.hidden = hidden;
  }

  @Override
  protected String getDefaultIdPrefix() {
    return "file";
  }
}
