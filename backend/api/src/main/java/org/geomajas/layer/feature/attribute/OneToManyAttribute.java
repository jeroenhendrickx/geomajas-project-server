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

package org.geomajas.layer.feature.attribute;

import java.util.ArrayList;
import java.util.List;

import org.geomajas.configuration.AssociationType;
import org.geomajas.global.Api;

/**
 * <p>
 * Definition of the one-to-many association attribute. This type of attribute is not a simple primitive attribute with
 * a single value, but instead holds the values of an entire list of beans. This list of bean values has been defined in
 * the form of a list of {@link AssociationValue} objects.
 * </p>
 * 
 * @author Pieter De Graef
 * @since 1.6.0
 */
@Api(allMethods = true)
public class OneToManyAttribute extends AssociationAttribute<List<AssociationValue>> {

	private static final long serialVersionUID = 154L;

	private List<AssociationValue> value;

	// Constructors:

	/**
	 * Create attribute without value (needed for GWT).
	 */
	public OneToManyAttribute() {
	}

	/**
	 * Create attribute with specified value.
	 * 
	 * @param value
	 *            value for attribute
	 */
	public OneToManyAttribute(List<AssociationValue> value) {
		this.value = value;
	}

	/**
	 * {@inheritDoc}
	 */
	public AssociationType getType() {
		return AssociationType.ONE_TO_MANY;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AssociationValue> getValue() {
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEmpty() {
		return value == null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(List<AssociationValue> value) {
		this.value = value;
	}

	/**
	 * Create a clone of this attribute object.
	 * 
	 * @since 1.7.0
	 * @return A copy of this ManyToOne attribute.
	 */
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "CN_IDIOM_NO_SUPER_CALL",
			justification = "needed for GWT")
	public Object clone() {
		OneToManyAttribute clone = new OneToManyAttribute();
		if (value != null) {
			List<AssociationValue> clones = new ArrayList<AssociationValue>();
			for (AssociationValue v : value) {
				clones.add((AssociationValue) v.clone());
			}
			clone.setValue(clones);
		}
		clone.setEditable(isEditable());
		return clone;
	}
}
