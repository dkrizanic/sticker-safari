import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiService, { CollectorResponse } from '../../api/apiService';
import { useToast } from '../../context/ToastContext';
import AdminTable from './shared/AdminTable';
import FormModal from './shared/FormModal';
import DeleteConfirmModal from './shared/DeleteConfirmModal';

const CollectorManagement: React.FC = () => {
  const navigate = useNavigate();
  const { addToast } = useToast();
  const [collectors, setCollectors] = useState<CollectorResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [selectedCollector, setSelectedCollector] = useState<CollectorResponse | null>(null);
  const [formData, setFormData] = useState({ username: '' });
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    loadCollectors();
  }, []);

  const loadCollectors = async () => {
    try {
      setLoading(true);
      const data = await apiService.getAllCollectors();
      setCollectors(data);
    } catch (error) {
      addToast('Failed to load collectors', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (collector: CollectorResponse) => {
    setSelectedCollector(collector);
    setFormData({ username: collector.username });
    setShowEditModal(true);
  };

  const handleDelete = (collector: CollectorResponse) => {
    setSelectedCollector(collector);
    setShowDeleteModal(true);
  };

  const handleSubmitEdit = async () => {
    if (!selectedCollector || !formData.username.trim()) {
      addToast('Username is required', 'error');
      return;
    }

    try {
      setIsSubmitting(true);
      await apiService.updateCollector(selectedCollector.id, formData.username);
      addToast('Collector updated successfully', 'success');
      setShowEditModal(false);
      loadCollectors();
    } catch (error) {
      addToast('Failed to update collector', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleConfirmDelete = async () => {
    if (!selectedCollector) return;

    try {
      setIsSubmitting(true);
      await apiService.deleteCollector(selectedCollector.id);
      addToast('Collector deleted successfully', 'success');
      setShowDeleteModal(false);
      loadCollectors();
    } catch (error) {
      addToast('Failed to delete collector', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const columns = [
    { key: 'id', header: 'ID' },
    { key: 'username', header: 'Username' }
  ];

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-xl text-gray-600">Loading collectors...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-100 via-blue-50 to-pink-100 p-8">
      <div className="max-w-6xl mx-auto">
        <div className="flex justify-between items-center mb-8">
          <div>
            <button
              onClick={() => navigate('/admin')}
              className="text-blue-600 hover:text-blue-800 mb-2 flex items-center"
            >
              ← Back to Dashboard
            </button>
            <h1 className="text-4xl font-bold text-gray-900">Collector Management</h1>
            <p className="text-gray-600 mt-2">
              Note: New collectors are created through the registration process
            </p>
          </div>
        </div>

        <AdminTable
          columns={columns}
          data={collectors}
          onEdit={handleEdit}
          onDelete={handleDelete}
          emptyMessage="No collectors found."
        />

        {/* Edit Modal */}
        <FormModal
          isOpen={showEditModal}
          title="Edit Collector"
          onClose={() => setShowEditModal(false)}
          onSubmit={handleSubmitEdit}
          isSubmitting={isSubmitting}
          submitText="Update"
        >
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Username *
            </label>
            <input
              type="text"
              value={formData.username}
              onChange={(e) => setFormData({ username: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Enter username"
              required
            />
          </div>
          <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-3">
            <p className="text-sm text-yellow-800">
              <strong>Warning:</strong> Changing the username will affect the collector's login credentials.
            </p>
          </div>
        </FormModal>

        {/* Delete Confirmation Modal */}
        <DeleteConfirmModal
          isOpen={showDeleteModal}
          title="Delete Collector"
          message={`Are you sure you want to delete collector "${selectedCollector?.username}"? This will remove all their stickers and trade history. This action cannot be undone.`}
          onConfirm={handleConfirmDelete}
          onCancel={() => setShowDeleteModal(false)}
          isDeleting={isSubmitting}
        />
      </div>
    </div>
  );
};

export default CollectorManagement;
