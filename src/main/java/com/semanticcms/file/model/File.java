/*
 * semanticcms-file-model - Files nested within SemanticCMS pages and elements.
 * Copyright (C) 2013, 2014, 2015, 2016  AO Industries, Inc.
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

import com.semanticcms.core.model.Element;

public class File extends Element {

	/**
	 * The path separator used for all file references.
	 */
	public static final char SEPARATOR_CHAR = '/';

	/**
	 * The path separator as a String.
	 */
	public static final String SEPARATOR_STRING = Character.toString(SEPARATOR_CHAR);

	private String book;
	private String path;
	private boolean hidden;

	@Override
	public File freeze() {
		super.freeze();
		return this;
	}

	/**
	 * The label is always the filename.
	 */
	@Override
	public String getLabel() {
		if(path != null) {
			int slashBefore;
			if(path.endsWith(SEPARATOR_STRING)) {
				slashBefore = path.lastIndexOf(SEPARATOR_CHAR, path.length() - 2);
			} else {
				slashBefore = path.lastIndexOf(SEPARATOR_CHAR);
			}
			String filename = path.substring(slashBefore + 1);
			if(filename.isEmpty()) throw new IllegalArgumentException("Invalid filename for file: " + path);
			return filename;
		}
		throw new IllegalStateException("Path not set");
	}

	public String getBook() {
		return book;
	}

	public void setBook(String book) {
		checkNotFrozen();
		this.book = book==null || book.isEmpty() ? null : book;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		checkNotFrozen();
		this.path = path==null || path.isEmpty() ? null : path;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		checkNotFrozen();
		this.hidden = hidden;
	}

	@Override
	public String getListItemCssClass() {
		// TODO: Multiple classes based on file type (from extension or mime type/magic?)
		if(path.endsWith(SEPARATOR_STRING)) {
			return "semanticcms-file-list-item-directory";
		} else {
			return "semanticcms-file-list-item-file";
		}
	}

	@Override
	protected String getDefaultIdPrefix() {
		return "file";
	}

	@Override
	public String getLinkCssClass() {
		// TODO: Multiple classes based on file type (from extension or mime type/magic?)
		if(path.endsWith(SEPARATOR_STRING)) {
			return "semanticcms-file-directory-link";
		} else {
			return "semanticcms-file-file-link";
		}
	}
}
