/*
 * This file is part of Geomajas, a component framework for building
 * rich Internet applications (RIA) with sophisticated capabilities for the
 * display, analysis and management of geographic information.
 * It is a building block that allows developers to add maps
 * and other geographic data capabilities to their web applications.
 *
 * Copyright 2008-2010 Geosparc, http://www.geosparc.com, Belgium
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.geomajas.gwt.client.action.toolbar;

import org.geomajas.gwt.client.action.ToolbarModalAction;
import org.geomajas.gwt.client.controller.PanController;
import org.geomajas.gwt.client.i18n.I18nProvider;
import org.geomajas.gwt.client.widget.MapWidget;

import com.smartgwt.client.widgets.events.ClickEvent;

/**
 * Pan tool, allows dragging the map too pan/scroll, or selecting an area to zoom to (by pressing shift or control).
 *
 * @author Joachim Van der Auwera
 */
public class PanModalAction extends ToolbarModalAction {

	private MapWidget mapWidget;

	private PanController controller;

	public PanModalAction(MapWidget mapWidget) {
		super("[ISOMORPHIC]/geomajas/osgeo/pan.png", I18nProvider.getToolbar().pan());
		this.mapWidget = mapWidget;
		controller = new PanController(mapWidget);
	}

	@Override
	public void onSelect(ClickEvent event) {
		mapWidget.setController(controller);
	}

	@Override
	public void onDeselect(ClickEvent event) {
		mapWidget.setController(null);
	}
}
