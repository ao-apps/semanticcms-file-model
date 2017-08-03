/*
 * semanticcms-file-model - Files nested within SemanticCMS pages and elements.
 * Copyright (C) 2013, 2014, 2015, 2016, 2017  AO Industries, Inc.
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
 * along with semanticcms-file-model.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.semanticcms.file.model;

import com.aoindustries.util.StringUtility;
import com.aoindustries.util.WrappedException;
import com.semanticcms.core.model.Element;
import com.semanticcms.core.model.Resource;
import com.semanticcms.core.model.ResourceRef;
import com.semanticcms.core.model.ResourceStore;
import java.io.FileNotFoundException;
import java.io.IOException;

public class File extends Element {

	/**
	 * The path separator used for all file references.
	 */
	public static final char SEPARATOR_CHAR = '/';

	/**
	 * The path separator as a String.
	 */
	public static final String SEPARATOR_STRING = Character.toString(SEPARATOR_CHAR);

	private final ResourceStore resourceStore;

	private volatile ResourceRef resourceRef;
	private volatile boolean hidden;

	public File(ResourceStore resourceStore) {
		this.resourceStore = resourceStore;
	}

	/**
	 * Does not include the size on the ID template, also strips any file extension if it will not leave the filename empty.
	 */
	@Override
	protected String getElementIdTemplate() {
		ResourceRef rr = getResourceRef();
		if(rr != null) {
			String path = rr.getPath();
			int slashBefore;
			if(path.endsWith(SEPARATOR_STRING)) {
				slashBefore = path.lastIndexOf(SEPARATOR_CHAR, path.length() - 2);
			} else {
				slashBefore = path.lastIndexOf(SEPARATOR_CHAR);
			}
			String filename = path.substring(slashBefore + 1);
			if(filename.isEmpty()) throw new IllegalArgumentException("Invalid filename for file: " + path);
			// Strip extension if will not leave empty
			int lastDot = filename.lastIndexOf('.');
			if(lastDot > 0) filename = filename.substring(0, lastDot);
			return filename;
		}
		throw new IllegalStateException("Path not set");
	}

	/**
	 * The label is always the filename.
	 */
	@Override
	public String getLabel() {
		ResourceRef rr = getResourceRef();
		if(rr != null) {
			String path = rr.getPath();
			boolean isDirectory = path.endsWith(SEPARATOR_STRING);
			int slashBefore;
			if(isDirectory) {
				slashBefore = path.lastIndexOf(SEPARATOR_CHAR, path.length() - 2);
			} else {
				slashBefore = path.lastIndexOf(SEPARATOR_CHAR);
			}
			String filename = path.substring(slashBefore + 1);
			if(filename.isEmpty()) throw new IllegalArgumentException("Invalid filename for file: " + path);
			if(!isDirectory && resourceStore != null) {
				try {
					Resource resource = resourceStore.getResource(rr);
					if(resource.exists()) {
						return
							filename
							+ " ("
							+ StringUtility.getApproximateSize(resource.getLength())
							+ ')'
						;
					}
				} catch(FileNotFoundException e) {
					// Resource removed between calls to exists() and getLength()
					// fall-through to return filename
				} catch(IOException e) {
					throw new WrappedException(e);
				}
			}
			return filename;
		}
		throw new IllegalStateException("Path not set");
	}

	public ResourceRef getResourceRef() {
		return resourceRef;
	}

	public void setResourceRef(ResourceRef resourceRef) {
		checkNotFrozen();
		this.resourceRef = resourceRef;
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
