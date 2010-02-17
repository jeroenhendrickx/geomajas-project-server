dojo.provide("geomajas.widget.FeatureDetailTable");
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
dojo.require("dijit.layout.ContentPane");
dojo.require("dijit._Templated");
dojo.require("dojox.grid.Grid");
dojo.require("dojox.grid._data.model");
dojo.require("dojo.parser");
dojo.requireLocalization("geomajas.widget", "featureDetailTable");

dojo.declare("geomajas.widget.FeatureDetailTable", [dijit.layout.ContentPane, dijit._Templated], {

	widgetsInTemplate : true,
	templatePath : dojo.moduleUrl("geomajas.widget", "html/FeatureDetailTable.html"),

	feature : null,
	editorFactory : null,
	formatterFactory: null,
	isOnlyEditor: false,
	ids : [],
	
	attributeString : "",
	valueString : "",

	postMixInProperties : function(/*Object*/args, /*Object*/frag, /*Widget*/parent){
		var widgetLocale = dojo.i18n.getLocalization("geomajas.widget", "featureDetailTable");
		this.attributeString = widgetLocale.attribute;
		this.valueString = widgetLocale.value;
	},
	
	constructor : function () {
		this.editorFactory = new AttributeEditorFactory();
		this.formatterFactory = new AttributeFormatterFactory();
	},

	setFeature : function (feature) {
		if (dojo.byId(this.id+":tbody") == null) {
			log.error ("FeatureDetailTable: trying to set feature while the " +
					"widget is not yet initialized!");
		}
		this.reset();
		this.feature = feature;

		if (feature != null) {
			var attr = this.feature.getLayer().getFeatureType().getVisibleAttributes();
			var keys = attr.getKeyList();
			for (var i=0; i<keys.length; i++) {
				var atDef = attr.item(keys[i]);
				this._addAttribute(atDef);
			}
		}
	},

	getFeature : function () {
		var attr = this.feature.getLayer().getFeatureType().getVisibleAttributes();
		var keys = attr.getKeyList();
		for (var i=0; i < keys.length; i++) {
			var atDef = attr.item(keys[i]);
			var id = this._createEditorId(atDef);
			this.feature.setAttributeValue(atDef.getName(),this.editorFactory.getEditorValue(id,atDef));
		}
		return this.feature;
	},

	reset : function () {
		var tbody = dojo.byId(this.id+":tbody");
		if (tbody != null) {
			while (tbody.firstChild){
				tbody.removeChild(tbody.firstChild);
			}
		}
	},

	isValid : function () {
		for (var i=0; i<this.ids.length; i++) {
			var editor = dijit.byId(this.ids[i]);
			if (editor && editor.isValid) {
				if (!editor.isValid()) {
					log.error ("Editor "+editor.id+" is not valid...");
					return false;
				}
			}
		}
		return true;
	},

	onChange : function () {
	},

	//-------------------------------------------------------------------------
	// Private functions.
	//-------------------------------------------------------------------------

	/**
	 * @private
	 * Adds a new row to the table, displaying an attribute.
	 */
	_addAttribute : function (atDef) {
		// First TD : the attribute's label. 
		var td1 = document.createElement("td");
		dojo.addClass(td1, "fdtTD");
		var txt1 = document.createTextNode(atDef.getLabel());
		td1.appendChild(txt1);

		// Second TD : an appropriate editor or displayer
		var td2 = document.createElement("td");
		dojo.addClass(td2, "fdtTD");
		if (!this.isOnlyEditor && !atDef.isEditable()) {
			var value = this._createValue(atDef);
			var formatter = this.formatterFactory.create(atDef);
			var div = document.createElement("div");
			div.setAttribute("class", "dijit dijitReset dijitInlineTable dijitLeft dijitTextBox dijitTextBoxDisabled dijitDisabled");
			div.innerHTML = formatter(value);
			td2.appendChild(div);
		} else {
			var id = this._createEditorId(atDef);
			if (dijit.byId(id)) {
				dijit.byId(id).destroy();
			}
			var editor = this.editorFactory.create(id , atDef, this.feature, document.createElement("div"));
			this.ids.push(id);
			if (!atDef.isEditable()) {
				editor.setDisabled(true);
			}
			this.connect(editor, "onChange", dojo.hitch(this, "onChange"));
			if (editor.validate) {
				this.connect(editor, "validate", dojo.hitch(this, "onChange"));
			}
			td2.appendChild(editor.domNode);
		}
		// Create the TR out of the TD's:
		var tr = document.createElement("tr");
		tr.appendChild(td1);
		tr.appendChild(td2);

		var tbody = dojo.byId(this.id+":tbody");
		tbody.appendChild(tr);
	},

	/**
	 * @private
	 */
	_textRow : function (atDef) {
		var temp = "<tr><td>"+atDef.getLabel()+"</td>";
		temp += "<td><div class='noBorderTable'><div id="+this._createEditorId(atDef)+"></div></div></td>";
		return temp;
	},

	/**
	 * @private
	 */
	_createEditorId : function (atDef) {
		if (this.feature != null) {
			return "atribute:editor:" + this.feature.getId() + ":" + atDef.getName();
		} else {
			return "atribute:editor:" + atDef.getName();
		}
	},
	_createValue : function (atDef) {
		var primitive = "";
		if (this.feature != null) {
			var value = this.feature.getAttributeValue(atDef.getName());
			primitive = atDef.getPrimitiveValue(value);
		}
		return primitive;
	}
});
