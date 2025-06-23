"""
Investigation Validator - Python Implementation

This module provides a Python implementation of the Kotlin investigation validation logic
from InvestigationGenerator.kt. It maintains exact business logic while following Python
conventions and best practices.
"""

from typing import Dict, List, Optional, Any, Tuple
from dataclasses import dataclass
from enum import Enum
import logging

# Constants
class ViewType(Enum):
    """View type constants equivalent to Kotlin ViewType"""
    FORM_EDITTEXT = "EditText"

class ValidationError(Exception):
    """Custom exception for validation errors"""
    pass

@dataclass
class FormLayout:
    """Python equivalent of Kotlin FormLayout data class"""
    view_type: str
    id: str
    title: str
    is_mandatory: bool = False
    min_length: Optional[int] = None
    max_length: Optional[int] = None
    min_value: Optional[float] = None
    max_value: Optional[float] = None
    content_length: Optional[int] = None
    unit_list: Optional[List[Dict[str, Any]]] = None

@dataclass
class InvestigationModel:
    """Python equivalent of Kotlin InvestigationModel data class"""
    test_name: str
    recommended_by: str
    recommended_on: str
    result_hash_map: Optional[Dict[str, Any]] = None
    data_error: bool = True
    error_message: Optional[str] = None
    id: Optional[str] = None
    dropdown_state: bool = False
    result_list: Optional[Any] = None  # FormResponse equivalent

class InvestigationValidator:
    """
    Python implementation of investigation validation logic
    Maintains exact business logic from Kotlin version
    """
    
    def __init__(self, is_community: bool = False):
        self.is_community = is_community
        self.logger = logging.getLogger(__name__)
    
    def on_validate_input(self, is_lab_tech: bool, server_data: Optional[List[InvestigationModel]]) -> bool:
        """
        Validates investigation input data
        
        Args:
            is_lab_tech: Whether the user is a lab technician
            server_data: List of investigation models to validate
            
        Returns:
            bool: True if all validations pass, False otherwise
            
        Raises:
            ValidationError: When validation logic encounters critical errors
        """
        try:
            is_valid = True
            
            if server_data is None:
                self.logger.warning("Server data is None, validation failed")
                return False
            
            # Lab technician specific validation
            if is_lab_tech:
                any_results_entered = any(
                    investigation.result_hash_map is not None and investigation.result_hash_map
                    for investigation in server_data
                )
                
                if self.is_community or not any_results_entered:
                    for investigation in server_data:
                        if investigation.result_hash_map is None or not investigation.result_hash_map:
                            investigation.dropdown_state = not investigation.dropdown_state
                            # toggle_facility would be implemented here
                            investigation.error_message = "Please enter required data"
                            investigation.data_error = False
                            is_valid = False
            
            # General validation for all investigations
            filtered_investigations = [
                data for data in server_data
                if data.id is None or (data.result_hash_map is not None and len(data.result_hash_map) > 0)
            ]
            
            for data in filtered_investigations:
                if data.result_hash_map is not None and len(data.result_hash_map) == 0:
                    is_valid = True
                    data.error_message = None
                    data.data_error = True
                else:
                    # Validate form layout data
                    if data.result_list and hasattr(data.result_list, 'form_layout'):
                        for form_data in data.result_list.form_layout:
                            validation_result = self._validate_form_field(form_data, data)
                            if not validation_result:
                                is_valid = False
                                break
                    else:
                        is_valid = True
                        data.error_message = None
                        data.data_error = True
            
            return is_valid
            
        except Exception as e:
            self.logger.error(f"Validation error: {str(e)}")
            raise ValidationError(f"Validation failed: {str(e)}")
    
    def _validate_form_field(self, form_data: FormLayout, data: InvestigationModel) -> bool:
        """
        Validates individual form field
        
        Args:
            form_data: Form layout configuration
            data: Investigation data
            
        Returns:
            bool: True if field is valid, False otherwise
        """
        try:
            # Check mandatory field validation
            if self._is_mandatory_field_invalid(form_data, data):
                data.error_message = "Please enter required data"
                data.data_error = False
                return False
            
            # Validate edit text fields
            if form_data.view_type == ViewType.FORM_EDITTEXT.value:
                return self._validate_edit_text_field(form_data, data)
            
            return True
            
        except Exception as e:
            self.logger.error(f"Field validation error: {str(e)}")
            return False
    
    def _is_mandatory_field_invalid(self, form_data: FormLayout, data: InvestigationModel) -> bool:
        """
        Checks if mandatory field is invalid
        
        Args:
            form_data: Form layout configuration
            data: Investigation data
            
        Returns:
            bool: True if mandatory field is invalid
        """
        if not form_data.is_mandatory:
            return False
        
        if data.result_hash_map is None:
            return True
        
        # Check if field is missing
        if form_data.id not in data.result_hash_map:
            return True
        
        # Check if field is empty string
        value = data.result_hash_map.get(form_data.id)
        if isinstance(value, str) and not value.strip():
            return True
        
        return False
    
    def _validate_edit_text_field(self, form_data: FormLayout, data: InvestigationModel) -> bool:
        """
        Validates edit text field with min/max length and value constraints
        
        Args:
            form_data: Form layout configuration
            data: Investigation data
            
        Returns:
            bool: True if field is valid
        """
        if data.result_hash_map is None or form_data.id not in data.result_hash_map:
            return True
        
        actual_value = data.result_hash_map[form_data.id]
        
        # Skip validation for empty non-mandatory fields
        if isinstance(actual_value, str) and not actual_value.strip() and not form_data.is_mandatory:
            return True
        
        # Validate min/max length and values
        evaluation = self._validate_min_max_length(actual_value, form_data)
        unit_valid = self._validate_unit(form_data, data)
        
        is_valid = evaluation[0] and unit_valid
        
        if evaluation[1]:  # Error message exists
            data.error_message = f"{evaluation[1]} ({form_data.title})"
        
        data.data_error = is_valid
        return is_valid
    
    def _validate_unit(self, form_data: FormLayout, data: InvestigationModel) -> bool:
        """
        Validates unit selection for form field
        
        Args:
            form_data: Form layout configuration
            data: Investigation data
            
        Returns:
            bool: True if unit is valid or not required
        """
        if form_data.unit_list is None or len(form_data.unit_list) == 0:
            return True
        
        unit_key = f"{form_data.id}_unit"
        return data.result_hash_map is not None and unit_key in data.result_hash_map
    
    def _validate_min_max_length(
        self, 
        actual_value: Any, 
        form_data: FormLayout
    ) -> Tuple[bool, Optional[str]]:
        """
        Validates minimum/maximum length and value constraints
        
        Args:
            actual_value: The value to validate
            form_data: Form layout configuration
            
        Returns:
            Tuple[bool, Optional[str]]: (is_valid, error_message)
        """
        is_valid = True
        error_message = None
        
        try:
            # Validate minimum length for strings
            if (form_data.min_length is not None and 
                form_data.view_type == ViewType.FORM_EDITTEXT.value and
                isinstance(actual_value, str) and
                len(actual_value) < form_data.min_length):
                
                error_message = f"Minimum length required: {form_data.min_length}"
                is_valid = False
            
            # Validate min/max values for numeric fields
            elif form_data.max_value is not None or form_data.min_value is not None:
                numeric_value = self._convert_to_float(actual_value)
                if numeric_value is not None:
                    if form_data.max_value is not None and form_data.min_value is not None:
                        if numeric_value < form_data.min_value or numeric_value > form_data.max_value:
                            error_message = f"Value must be between {form_data.min_value} and {form_data.max_value}"
                            is_valid = False
                    elif form_data.min_value is not None:
                        if numeric_value < form_data.min_value:
                            error_message = f"Minimum value required: {form_data.min_value}"
                            is_valid = False
                    elif form_data.max_value is not None:
                        if numeric_value > form_data.max_value:
                            error_message = f"Maximum value allowed: {form_data.max_value}"
                            is_valid = False
            
            # Validate content length
            elif form_data.content_length is not None:
                value_str = str(actual_value)
                if len(value_str) != form_data.content_length:
                    error_message = f"Length must be exactly {form_data.content_length} characters"
                    is_valid = False
            
        except Exception as e:
            self.logger.error(f"Min/max validation error: {str(e)}")
            error_message = "Validation error occurred"
            is_valid = False
        
        return is_valid, error_message
    
    def _convert_to_float(self, value: Any) -> Optional[float]:
        """
        Safely converts value to float
        
        Args:
            value: Value to convert
            
        Returns:
            Optional[float]: Converted value or None if conversion fails
        """
        try:
            if isinstance(value, (int, float)):
                return float(value)
            elif isinstance(value, str):
                return float(value)
            else:
                return None
        except (ValueError, TypeError):
            return None 