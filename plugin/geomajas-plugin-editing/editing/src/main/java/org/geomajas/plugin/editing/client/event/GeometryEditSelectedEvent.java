/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2011 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the GNU Affero
 * General Public License. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */

package org.geomajas.plugin.editing.client.event;

import java.util.List;

import org.geomajas.annotation.FutureApi;
import org.geomajas.geometry.Geometry;
import org.geomajas.plugin.editing.client.service.GeometryIndex;

/**
 * Event which is passed when a part of a geometry is selected during geometry editing.
 * 
 * @author Pieter De Graef
 * @since 1.0.0
 */
@FutureApi(allMethods = true)
public class GeometryEditSelectedEvent extends GeometryEditEvent<GeometryEditSelectionHandler> {

	public GeometryEditSelectedEvent(Geometry geometry, List<GeometryIndex> indices) {
		super(geometry, indices);
	}

	public Type<GeometryEditSelectionHandler> getAssociatedType() {
		return GeometryEditSelectionHandler.TYPE;
	}

	protected void dispatch(GeometryEditSelectionHandler featureSelectionHandler) {
		featureSelectionHandler.onGeometryEditSelected(this);
	}
}