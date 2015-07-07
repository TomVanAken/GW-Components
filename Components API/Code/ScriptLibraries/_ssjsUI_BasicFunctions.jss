/***
 *	  Adds an errormessage for a specific component.
 *	  Errors are shown in DisplayError or DisplayErrors components on an xpage
 ***/
function addErrorMessage(sComponentId, sMessage) {
	var fldComponent = sComponentId!=null && sComponentId!=""?getComponent(sComponentId):null;
	var sErrorMessage = "";
	var sComponentClientId = null;
	if(fldComponent==null) {
		//if component not found, just add message.
		sErrorMessage = sMessage;
	} else {
		sComponentClientId = fldComponent.getClientId(facesContext);
		sComponentClientId = sComponentClientId==""?null:sComponentClientId;
		//try to find label
		var lblComponent = getLabelFor(fldComponent);
		if(lblComponent==null) {
			sErrorMessage = sComponentId + " " + sMessage;
		} else {
			sErrorMessage = lblComponent.getValue() + " " + sMessage;
		}
	}
	var msgErrorMessage:javax.faces.application.FacesMessage = new javax.faces.application.FacesMessage();
	msgErrorMessage.setSeverity(javax.faces.application.FacesMessage.SEVERITY_ERROR);
	msgErrorMessage.setDetail(sErrorMessage);
	msgErrorMessage.setSummary(sErrorMessage);
	facesContext.addMessage(sComponentClientId, msgErrorMessage);
}


/**
 * get the component triggering the update
 */
function getTriggeringComponent() {
	try {
		  var eventHandlerClientId = param.get( '$$xspsubmitid' );
			
		  var eventHandlerId = @RightBack( eventHandlerClientId, ':' );
		  var eventHandler = getComponent( eventHandlerId );  
		  if( !eventHandler ){ return null; }
		  
		  var parentComponent = eventHandler.getParent();
		  return parentComponent;  
		 } catch( e ){
			 print(e.toString());
			 return null;
		}
}

/**
 * Returns true if the id of the triggering component is componentId
 */
function submittedBy( componentId ){
 try {
  var parentComponent = getTriggeringComponent();
  if( !parentComponent ){ return false; }
  return ( parentComponent.getId() === componentId );  
 } catch( e ){ /*Debug.logException( e );*/ print(e.toString())}
}

/**
 * Returns true if the triggering component is contained by the panel with id panelId
 */
function submittedInPanel(panelId) {
	var parentComponent = getTriggeringComponent();
	while(parentComponent) {
		if(
			typeof(parentComponent)=="com.ibm.xsp.component.UIPanelEx"
			&& parentComponent.getId()==panelId
		) return true;  //return true if stated panel is found 
		
		
		//Get next parent component
		parentComponent = parentComponent.getParent();
	}
	return false;
}