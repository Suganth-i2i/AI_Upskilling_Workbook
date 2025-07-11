import unittest
from unittest.mock import Mock, patch
from investigation_validator import InvestigationValidator, InvestigationModel, FormLayout, ViewType

class TestInvestigationValidator(unittest.TestCase):
    """Test cases for InvestigationValidator class"""
    
    def setUp(self):
        """Set up test fixtures before each test method"""
        self.validator = InvestigationValidator(is_community=False)
    
    def test_validate_input_with_none_server_data(self):
        """Test validation with None server data"""
        result = self.validator.on_validate_input(False, None)
        self.assertFalse(result)
    
    def test_validate_input_with_empty_server_data(self):
        """Test validation with empty server data"""
        result = self.validator.on_validate_input(False, [])
        self.assertTrue(result)
    
    def test_lab_tech_validation_with_empty_results(self):
        """Test lab technician validation when no results are entered"""
        investigation = InvestigationModel(
            test_name="Test",
            recommended_by="Doctor",
            recommended_on="2024-01-01",
            result_hash_map={}
        )
        
        result = self.validator.on_validate_input(True, [investigation])
        self.assertFalse(result)
        self.assertEqual(investigation.error_message, "Please enter required data")
        self.assertFalse(investigation.data_error)
    
    def test_lab_tech_validation_with_results_entered(self):
        """Test lab technician validation when results are entered"""
        investigation = InvestigationModel(
            test_name="Test",
            recommended_by="Doctor",
            recommended_on="2024-01-01",
            result_hash_map={"test_field": "value"}
        )
        
        result = self.validator.on_validate_input(True, [investigation])
        self.assertTrue(result)
    
    def test_mandatory_field_validation_missing(self):
        """Test mandatory field validation when field is missing"""
        form_data = FormLayout(
            view_type=ViewType.FORM_EDITTEXT.value,
            id="test_field",
            title="Test Field",
            is_mandatory=True
        )
        
        investigation = InvestigationModel(
            test_name="Test",
            recommended_by="Doctor",
            recommended_on="2024-01-01",
            result_hash_map={}
        )
        
        # Mock result_list with form_layout
        investigation.result_list = Mock()
        investigation.result_list.form_layout = [form_data]
        
        result = self.validator.on_validate_input(False, [investigation])
        self.assertFalse(result)
        self.assertEqual(investigation.error_message, "Please enter required data")
    
    def test_mandatory_field_validation_empty_string(self):
        """Test mandatory field validation with empty string"""
        form_data = FormLayout(
            view_type=ViewType.FORM_EDITTEXT.value,
            id="test_field",
            title="Test Field",
            is_mandatory=True
        )
        
        investigation = InvestigationModel(
            test_name="Test",
            recommended_by="Doctor",
            recommended_on="2024-01-01",
            result_hash_map={"test_field": ""}
        )
        
        investigation.result_list = Mock()
        investigation.result_list.form_layout = [form_data]
        
        result = self.validator.on_validate_input(False, [investigation])
        self.assertFalse(result)
    
    def test_mandatory_field_validation_valid(self):
        """Test mandatory field validation with valid data"""
        form_data = FormLayout(
            view_type=ViewType.FORM_EDITTEXT.value,
            id="test_field",
            title="Test Field",
            is_mandatory=True
        )
        
        investigation = InvestigationModel(
            test_name="Test",
            recommended_by="Doctor",
            recommended_on="2024-01-01",
            result_hash_map={"test_field": "valid_value"}
        )
        
        investigation.result_list = Mock()
        investigation.result_list.form_layout = [form_data]
        
        result = self.validator.on_validate_input(False, [investigation])
        self.assertTrue(result)
    
    def test_min_length_validation(self):
        """Test minimum length validation"""
        form_data = FormLayout(
            view_type=ViewType.FORM_EDITTEXT.value,
            id="test_field",
            title="Test Field",
            min_length=5
        )
        
        investigation = InvestigationModel(
            test_name="Test",
            recommended_by="Doctor",
            recommended_on="2024-01-01",
            result_hash_map={"test_field": "abc"}
        )
        
        investigation.result_list = Mock()
        investigation.result_list.form_layout = [form_data]
        
        result = self.validator.on_validate_input(False, [investigation])
        self.assertFalse(result)
        self.assertIn("Minimum length required: 5", investigation.error_message)
    
    def test_min_length_validation_passed(self):
        """Test minimum length validation with valid length"""
        form_data = FormLayout(
            view_type=ViewType.FORM_EDITTEXT.value,
            id="test_field",
            title="Test Field",
            min_length=3
        )
        
        investigation = InvestigationModel(
            test_name="Test",
            recommended_by="Doctor",
            recommended_on="2024-01-01",
            result_hash_map={"test_field": "abc"}
        )
        
        investigation.result_list = Mock()
        investigation.result_list.form_layout = [form_data]
        
        result = self.validator.on_validate_input(False, [investigation])
        self.assertTrue(result)
    
    def test_min_max_value_validation(self):
        """Test minimum and maximum value validation"""
        form_data = FormLayout(
            view_type=ViewType.FORM_EDITTEXT.value,
            id="test_field",
            title="Test Field",
            min_value=10.0,
            max_value=100.0
        )
        
        investigation = InvestigationModel(
            test_name="Test",
            recommended_by="Doctor",
            recommended_on="2024-01-01",
            result_hash_map={"test_field": "5.0"}
        )
        
        investigation.result_list = Mock()
        investigation.result_list.form_layout = [form_data]
        
        result = self.validator.on_validate_input(False, [investigation])
        self.assertFalse(result)
        self.assertIn("Value must be between 10.0 and 100.0", investigation.error_message)
    
    def test_min_max_value_validation_passed(self):
        """Test minimum and maximum value validation with valid value"""
        form_data = FormLayout(
            view_type=ViewType.FORM_EDITTEXT.value,
            id="test_field",
            title="Test Field",
            min_value=10.0,
            max_value=100.0
        )
        
        investigation = InvestigationModel(
            test_name="Test",
            recommended_by="Doctor",
            recommended_on="2024-01-01",
            result_hash_map={"test_field": "50.0"}
        )
        
        investigation.result_list = Mock()
        investigation.result_list.form_layout = [form_data]
        
        result = self.validator.on_validate_input(False, [investigation])
        self.assertTrue(result)
    
    def test_min_value_validation_only(self):
        """Test minimum value validation only"""
        form_data = FormLayout(
            view_type=ViewType.FORM_EDITTEXT.value,
            id="test_field",
            title="Test Field",
            min_value=10.0
        )
        
        investigation = InvestigationModel(
            test_name="Test",
            recommended_by="Doctor",
            recommended_on="2024-01-01",
            result_hash_map={"test_field": "5.0"}
        )
        
        investigation.result_list = Mock()
        investigation.result_list.form_layout = [form_data]
        
        result = self.validator.on_validate_input(False, [investigation])
        self.assertFalse(result)
        self.assertIn("Minimum value required: 10.0", investigation.error_message)
    
    def test_max_value_validation_only(self):
        """Test maximum value validation only"""
        form_data = FormLayout(
            view_type=ViewType.FORM_EDITTEXT.value,
            id="test_field",
            title="Test Field",
            max_value=100.0
        )
        
        investigation = InvestigationModel(
            test_name="Test",
            recommended_by="Doctor",
            recommended_on="2024-01-01",
            result_hash_map={"test_field": "150.0"}
        )
        
        investigation.result_list = Mock()
        investigation.result_list.form_layout = [form_data]
        
        result = self.validator.on_validate_input(False, [investigation])
        self.assertFalse(result)
        self.assertIn("Maximum value allowed: 100.0", investigation.error_message)
    
    def test_unit_validation_required(self):
        """Test unit validation when required"""
        form_data = FormLayout(
            view_type=ViewType.FORM_EDITTEXT.value,
            id="test_field",
            title="Test Field",
            unit_list=[{"unit": "mg/dL"}]
        )
        
        investigation = InvestigationModel(
            test_name="Test",
            recommended_by="Doctor",
            recommended_on="2024-01-01",
            result_hash_map={"test_field": "50.0"}
        )
        
        investigation.result_list = Mock()
        investigation.result_list.form_layout = [form_data]
        
        result = self.validator.on_validate_input(False, [investigation])
        self.assertFalse(result)
    
    def test_unit_validation_passed(self):
        """Test unit validation when unit is provided"""
        form_data = FormLayout(
            view_type=ViewType.FORM_EDITTEXT.value,
            id="test_field",
            title="Test Field",
            unit_list=[{"unit": "mg/dL"}]
        )
        
        investigation = InvestigationModel(
            test_name="Test",
            recommended_by="Doctor",
            recommended_on="2024-01-01",
            result_hash_map={"test_field": "50.0", "test_field_unit": "mg/dL"}
        )
        
        investigation.result_list = Mock()
        investigation.result_list.form_layout = [form_data]
        
        result = self.validator.on_validate_input(False, [investigation])
        self.assertTrue(result)
    
    def test_unit_validation_not_required(self):
        """Test unit validation when not required"""
        form_data = FormLayout(
            view_type=ViewType.FORM_EDITTEXT.value,
            id="test_field",
            title="Test Field",
            unit_list=None
        )
        
        investigation = InvestigationModel(
            test_name="Test",
            recommended_by="Doctor",
            recommended_on="2024-01-01",
            result_hash_map={"test_field": "50.0"}
        )
        
        investigation.result_list = Mock()
        investigation.result_list.form_layout = [form_data]
        
        result = self.validator.on_validate_input(False, [investigation])
        self.assertTrue(result)
    
    def test_content_length_validation(self):
        """Test content length validation"""
        form_data = FormLayout(
            view_type=ViewType.FORM_EDITTEXT.value,
            id="test_field",
            title="Test Field",
            content_length=4
        )
        
        investigation = InvestigationModel(
            test_name="Test",
            recommended_by="Doctor",
            recommended_on="2024-01-01",
            result_hash_map={"test_field": "12345"}
        )
        
        investigation.result_list = Mock()
        investigation.result_list.form_layout = [form_data]
        
        result = self.validator.on_validate_input(False, [investigation])
        self.assertFalse(result)
        self.assertIn("Length must be exactly 4 characters", investigation.error_message)
    
    def test_content_length_validation_passed(self):
        """Test content length validation with correct length"""
        form_data = FormLayout(
            view_type=ViewType.FORM_EDITTEXT.value,
            id="test_field",
            title="Test Field",
            content_length=4
        )
        
        investigation = InvestigationModel(
            test_name="Test",
            recommended_by="Doctor",
            recommended_on="2024-01-01",
            result_hash_map={"test_field": "1234"}
        )
        
        investigation.result_list = Mock()
        investigation.result_list.form_layout = [form_data]
        
        result = self.validator.on_validate_input(False, [investigation])
        self.assertTrue(result)
    
    def test_empty_result_hash_map(self):
        """Test validation with empty result hash map"""
        investigation = InvestigationModel(
            test_name="Test",
            recommended_by="Doctor",
            recommended_on="2024-01-01",
            result_hash_map={}
        )
        
        result = self.validator.on_validate_input(False, [investigation])
        self.assertTrue(result)
        self.assertIsNone(investigation.error_message)
        self.assertTrue(investigation.data_error)
    
    def test_non_edit_text_field_validation(self):
        """Test validation for non-edit text fields"""
        form_data = FormLayout(
            view_type="Spinner",  # Non-edit text field
            id="test_field",
            title="Test Field",
            is_mandatory=True
        )
        
        investigation = InvestigationModel(
            test_name="Test",
            recommended_by="Doctor",
            recommended_on="2024-01-01",
            result_hash_map={"test_field": "value"}
        )
        
        investigation.result_list = Mock()
        investigation.result_list.form_layout = [form_data]
        
        result = self.validator.on_validate_input(False, [investigation])
        self.assertTrue(result)
    
    def test_community_user_validation(self):
        """Test validation for community users"""
        community_validator = InvestigationValidator(is_community=True)
        
        investigation = InvestigationModel(
            test_name="Test",
            recommended_by="Doctor",
            recommended_on="2024-01-01",
            result_hash_map={}
        )
        
        result = community_validator.on_validate_input(True, [investigation])
        self.assertFalse(result)
        self.assertEqual(investigation.error_message, "Please enter required data")
    
    def test_convert_to_float_with_valid_inputs(self):
        """Test _convert_to_float method with valid inputs"""
        # Test with integer
        result = self.validator._convert_to_float(42)
        self.assertEqual(result, 42.0)
        
        # Test with float
        result = self.validator._convert_to_float(42.5)
        self.assertEqual(result, 42.5)
        
        # Test with string
        result = self.validator._convert_to_float("42.5")
        self.assertEqual(result, 42.5)
    
    def test_convert_to_float_with_invalid_inputs(self):
        """Test _convert_to_float method with invalid inputs"""
        # Test with None
        result = self.validator._convert_to_float(None)
        self.assertIsNone(result)
        
        # Test with invalid string
        result = self.validator._convert_to_float("invalid")
        self.assertIsNone(result)
        
        # Test with list
        result = self.validator._convert_to_float([1, 2, 3])
        self.assertIsNone(result)
    
    def test_exception_handling(self):
        """Test exception handling in validation"""
        # Mock a scenario that would cause an exception
        with patch.object(self.validator, '_validate_min_max_length', side_effect=Exception("Test error")):
            form_data = FormLayout(
                view_type=ViewType.FORM_EDITTEXT.value,
                id="test_field",
                title="Test Field"
            )
            
            investigation = InvestigationModel(
                test_name="Test",
                recommended_by="Doctor",
                recommended_on="2024-01-01",
                result_hash_map={"test_field": "value"}
            )
            
            investigation.result_list = Mock()
            investigation.result_list.form_layout = [form_data]
            
            result = self.validator.on_validate_input(False, [investigation])
            self.assertFalse(result)

if __name__ == '__main__':
    unittest.main()
