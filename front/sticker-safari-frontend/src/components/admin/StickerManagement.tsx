import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiService, { StickerResponse, AlbumResponse } from '../../api/apiService';
import { useToast } from '../../context/ToastContext';
import AdminTable from './shared/AdminTable';
import FormModal from './shared/FormModal';

interface StickerFormData {
  name: string;
  latinName: string;
  description: string;
  habitat: string;
  src: string;
}

const StickerManagement: React.FC = () => {
  const navigate = useNavigate();
  const { addToast } = useToast();
  const [stickers, setStickers] = useState<StickerResponse[]>([]);
  const [albums, setAlbums] = useState<AlbumResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [selectedSticker, setSelectedSticker] = useState<StickerResponse | null>(null);
  const [formData, setFormData] = useState<StickerFormData>({
    name: '',
    latinName: '',
    description: '',
    habitat: '',
    src: ''
  });
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const [stickersData, albumsData] = await Promise.all([
        apiService.getAllStickers(),
        apiService.getAllAlbums()
      ]);
      setStickers(stickersData);
      setAlbums(albumsData);
    } catch (error) {
      addToast('Failed to load data', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setFormData({
      name: '',
      latinName: '',
      description: '',
      habitat: '',
      src: ''
    });
    setShowCreateModal(true);
  };

  const handleEdit = (sticker: StickerResponse) => {
    setSelectedSticker(sticker);
    setFormData({
      name: sticker.name,
      latinName: sticker.latinName,
      description: sticker.description,
      habitat: sticker.habitat,
      src: sticker.src
    });
    setShowEditModal(true);
  };

  const validateForm = (): boolean => {
    if (!formData.name.trim()) {
      addToast('Sticker name is required', 'error');
      return false;
    }
    if (!formData.latinName.trim()) {
      addToast('Latin name is required', 'error');
      return false;
    }
    if (!formData.description.trim()) {
      addToast('Description is required', 'error');
      return false;
    }
    if (!formData.habitat.trim()) {
      addToast('Habitat is required', 'error');
      return false;
    }
    if (!formData.src.trim()) {
      addToast('Image source is required', 'error');
      return false;
    }
    return true;
  };

  const handleSubmitCreate = async () => {
    if (!validateForm()) return;

    try {
      setIsSubmitting(true);
      await apiService.createSticker(formData);
      addToast('Sticker created successfully', 'success');
      setShowCreateModal(false);
      loadData();
    } catch (error) {
      addToast('Failed to create sticker', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleSubmitEdit = async () => {
    if (!selectedSticker || !validateForm()) return;

    try {
      setIsSubmitting(true);
      await apiService.updateSticker(selectedSticker.id, formData);
      addToast('Sticker updated successfully', 'success');
      setShowEditModal(false);
      loadData();
    } catch (error) {
      addToast('Failed to update sticker', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const columns = [
    { key: 'id', header: 'ID' },
    { key: 'name', header: 'Name' },
    { key: 'latinName', header: 'Latin Name' },
    {
      key: 'description',
      header: 'Description',
      render: (desc: string) => desc.length > 50 ? desc.substring(0, 50) + '...' : desc
    },
    { key: 'habitat', header: 'Habitat' }
  ];

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-xl text-gray-600">Loading stickers...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-100 via-blue-50 to-pink-100 p-8">
      <div className="max-w-7xl mx-auto">
        <div className="flex justify-between items-center mb-8">
          <div>
            <button
              onClick={() => navigate('/admin')}
              className="text-blue-600 hover:text-blue-800 mb-2 flex items-center"
            >
              ← Back to Dashboard
            </button>
            <h1 className="text-4xl font-bold text-gray-900">Sticker Management</h1>
            <p className="text-gray-600 mt-2">Note: Stickers cannot be deleted per requirements</p>
          </div>
          <button
            onClick={handleCreate}
            className="px-6 py-3 bg-gradient-to-r from-purple-600 to-blue-600 text-white rounded-lg hover:from-purple-700 hover:to-blue-700 transition-all shadow-lg"
          >
            + Create Sticker
          </button>
        </div>

        <AdminTable
          columns={columns}
          data={stickers}
          onEdit={handleEdit}
          emptyMessage="No stickers found. Create your first sticker!"
        />

        {/* Create Modal */}
        <FormModal
          isOpen={showCreateModal}
          title="Create New Sticker"
          onClose={() => setShowCreateModal(false)}
          onSubmit={handleSubmitCreate}
          isSubmitting={isSubmitting}
          submitText="Create"
        >
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Sticker Name *
            </label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="e.g., Lion"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Latin Name *
            </label>
            <input
              type="text"
              value={formData.latinName}
              onChange={(e) => setFormData({ ...formData, latinName: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="e.g., Panthera leo"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Description *
            </label>
            <textarea
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Enter sticker description"
              rows={3}
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Habitat *
            </label>
            <input
              type="text"
              value={formData.habitat}
              onChange={(e) => setFormData({ ...formData, habitat: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="e.g., African Savanna"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Image Source (URL) *
            </label>
            <input
              type="text"
              value={formData.src}
              onChange={(e) => setFormData({ ...formData, src: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="e.g., https://example.com/lion.jpg"
              required
            />
          </div>
        </FormModal>

        {/* Edit Modal */}
        <FormModal
          isOpen={showEditModal}
          title="Edit Sticker"
          onClose={() => setShowEditModal(false)}
          onSubmit={handleSubmitEdit}
          isSubmitting={isSubmitting}
          submitText="Update"
        >
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Sticker Name *
            </label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="e.g., Lion"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Latin Name *
            </label>
            <input
              type="text"
              value={formData.latinName}
              onChange={(e) => setFormData({ ...formData, latinName: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="e.g., Panthera leo"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Description *
            </label>
            <textarea
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Enter sticker description"
              rows={3}
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Habitat *
            </label>
            <input
              type="text"
              value={formData.habitat}
              onChange={(e) => setFormData({ ...formData, habitat: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="e.g., African Savanna"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Image Source (URL) *
            </label>
            <input
              type="text"
              value={formData.src}
              onChange={(e) => setFormData({ ...formData, src: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="e.g., https://example.com/lion.jpg"
              required
            />
          </div>
        </FormModal>
      </div>
    </div>
  );
};

export default StickerManagement;
