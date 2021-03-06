sap.ui.define(
  [
	  'sap/m/OverflowToolbar',
	  'sap/ui/model/json/JSONModel',
	  'sap/m/ToolbarDesign',
	  'sap/m/ToolbarStyle'],
  function(OverflowToolbar, JSONModel) {
  return sap.m.OverflowToolbar.extend(
		"io.rtdi.hanaappcontainer.editorapp.uieditorcontrols.OverflowToolbar", {
			metadata : {
				dnd : {
					draggable : true,
					droppable : true
				},
				properties: {
					propertiesModel: { type: "sap.ui.model.json.JSONModel", defaultValue: undefined },
					controlid: { type: "string", defaultValue: "" }
				},
				events : {
					showProperties : {}
				}
			},
			renderer : {},
			init : function() {
				sap.m.OverflowToolbar.prototype.init.apply(this, arguments);
				var oView = sap.ui.getCore().byId("mainview");
				var dropinfo = new sap.ui.core.dnd.DropInfo(
						{ 
							"groupName": "controls", 
							"dropPosition": sap.ui.core.dnd.DropPosition.OnOrBetween,
							"drop": oView.getController().onDropControl 
						}
				);
				var oModel = new JSONModel();
				oModel.setData({ "list": [
					{ "propertyname": "controlid" },
					{ "propertyname": "height" },
					{ "propertyname": "width" },
					{ "propertyname": "design" },
					{ "propertyname": "style" }
				] });
				this.setProperty("propertiesModel", oModel, true);

				this.addStyleClass("uieditor");
				this.insertDragDropConfig(dropinfo);
				this.attachBrowserEvent("dblclick", function(event) {
				    event.stopPropagation();
				    this.fireEvent("showProperties", undefined, true, false);
				    return false;
				}, this);			
				this.attachEvent("showProperties", sap.ui.getCore().byId("mainview").getController().showProperties);
			},
			getParentProperties : function() {
				return sap.m.OverflowToolbar.prototype.getMetadata.apply(this, arguments).getAllProperties();
			},
			getParentAggregations : function() {
				return sap.m.OverflowToolbar.prototype.getMetadata.apply(this, arguments).getAllAggregations();
			},
			getParentAssociations : function() {
				return sap.m.OverflowToolbar.prototype.getMetadata.apply(this, arguments).getAllAssociations();
			},
			getParentClassName : function() {
				return sap.m.OverflowToolbar.prototype.getMetadata.apply(this, arguments).getName();
			}
		});
});