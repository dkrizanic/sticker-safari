import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiService, { AlbumResponse, StickerResponse } from '../../api/apiService';
import { AlbumStatus } from '../../types';
import { useToast } from '../../context/ToastContext';
import AdminTable from './shared/AdminTable';
import FormModal from './shared/FormModal';
import DeleteConfirmModal from './shared/DeleteConfirmModal';

const AlbumManagement: React.FC = () => {
  const navigate = useNavigate();
  const { addToast } = useToast();
  const [albums, setAlbums] = useState<AlbumResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [selectedAlbum, setSelectedAlbum] = useState<AlbumResponse | null>(null);
  const [formData, setFormData] = useState({ name: '', status: AlbumStatus.PENDING });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [availableStickers, setAvailableStickers] = useState<StickerResponse[]>([]);
  const [selectedStickerIds, setSelectedStickerIds] = useState<number[]>([]);

  useEffect(() => {
    loadAlbums();
  }, []);

  const loadAlbums = async () => {
    try {
      setLoading(true);
      const data = await apiService.getAllAlbums();
      setAlbums(data);
    } catch (error) {
      addToast('Failed to load albums', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setFormData({ name: '', status: AlbumStatus.PENDING });
    setShowCreateModal(true);
  };

  const handleEdit = async (album: AlbumResponse) => {
    setSelectedAlbum(album);
    setFormData({ name: album.name, status: album.status });
    setSelectedStickerIds(album.stickers.map(s => s.id));
    
    // Fetch available stickers (without albums)
    try {
      const response = await apiService.getStickersPaginated(0, 1000, 'id', 'asc', '', -1);
      setAvailableStickers(response.stickers);
    } catch (error) {
      addToast('Failed to load available stickers', 'error');
    }
    
    setShowEditModal(true);
  };

  const handleDelete = (album: AlbumResponse) => {
    setSelectedAlbum(album);
    setShowDeleteModal(true);
  };

  const handleSubmitCreate = async () => {
    if (!formData.name.trim()) {
      addToast('Album name is required', 'error');
      return;
    }

    try {
      setIsSubmitting(true);
      await apiService.createAlbum(formData.name);
      addToast('Album created successfully', 'success');
      setShowCreateModal(false);
      loadAlbums();
    } catch (error) {
      addToast('Failed to create album', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleSubmitEdit = async () => {
    if (!selectedAlbum || !formData.name.trim()) {
      addToast('Album name is required', 'error');
      return;
    }

    // Validate: cannot publish album without stickers
    if (formData.status === AlbumStatus.PUBLISHED && selectedStickerIds.length === 0) {
      addToast('Cannot publish album without stickers', 'error');
      return;
    }

    try {
      setIsSubmitting(true);
      await apiService.updateAlbum(
        selectedAlbum.id, 
        formData.name, 
        formData.status,
        selectedStickerIds
      );
      addToast('Album updated successfully', 'success');
      setShowEditModal(false);
      loadAlbums();
    } catch (error) {
      addToast('Failed to update album', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleConfirmDelete = async () => {
    if (!selectedAlbum) return;

    try {
      setIsSubmitting(true);
      await apiService.deleteAlbum(selectedAlbum.id);
      addToast('Album deleted successfully', 'success');
      setShowDeleteModal(false);
      loadAlbums();
    } catch (error) {
      addToast('Failed to delete album', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const columns = [
    { key: 'id', header: 'ID' },
    { key: 'name', header: 'Album Name' },
    {
      key: 'status',
      header: 'Status',
      render: (status: string) => (
        <span className={`px-2 py-1 rounded-full text-sm font-semibold ${
          status === 'PUBLISHED' 
            ? 'bg-green-100 text-green-800' 
            : 'bg-yellow-100 text-yellow-800'
        }`}>
          {status}
        </span>
      )
    },
    {
      key: 'stickers',
      header: 'Stickers Count',
      render: (stickers: any[]) => stickers?.length || 0
    }
  ];

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-xl text-gray-600">Loading albums...</div>
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
            <h1 className="text-4xl font-bold text-gray-900">Album Management</h1>
          </div>
          <button
            onClick={handleCreate}
            className="px-6 py-3 bg-gradient-to-r from-purple-600 to-blue-600 text-white rounded-lg hover:from-purple-700 hover:to-blue-700 transition-all shadow-lg"
          >
            + Create Album
          </button>
        </div>

        <AdminTable
          columns={columns}
          data={albums}
          onEdit={handleEdit}
          onDelete={handleDelete}
          emptyMessage="No albums found. Create your first album!"
        />

        {/* Create Modal */}
        <FormModal
          isOpen={showCreateModal}
          title="Create New Album"
          onClose={() => setShowCreateModal(false)}
          onSubmit={handleSubmitCreate}
          isSubmitting={isSubmitting}
          submitText="Create"
        >
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Album Name *
            </label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Enter album name"
              required
            />
          </div>
        </FormModal>

        {/* Edit Modal */}
        <FormModal
          isOpen={showEditModal}
          title="Edit Album"
          onClose={() => setShowEditModal(false)}
          onSubmit={handleSubmitEdit}
          isSubmitting={isSubmitting}
          submitText="Update"
        >
          <div className="space-y-6">
            {/* Album Name */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Album Name *
              </label>
              <input
                type="text"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Enter album name"
                required
              />
            </div>

            {/* Status */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Status *
              </label>
              <select
                value={formData.status}
                onChange={(e) => setFormData({ ...formData, status: e.target.value as AlbumStatus })}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value={AlbumStatus.PENDING}>PENDING</option>
                <option value={AlbumStatus.PUBLISHED}>PUBLISHED</option>
              </select>
            </div>

            {/* Current Stickers */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Current Stickers ({selectedStickerIds.length})
              </label>
              <div className="border border-gray-300 rounded-lg p-3 max-h-48 overflow-y-auto bg-gray-50">
                {selectedAlbum?.stickers && selectedAlbum.stickers.filter(s => selectedStickerIds.includes(s.id)).length > 0 ? (
                  <div className="space-y-2">
                    {selectedAlbum.stickers
                      .filter(s => selectedStickerIds.includes(s.id))
                      .map(sticker => (
                        <div key={sticker.id} className="flex items-center justify-between bg-white p-2 rounded shadow-sm">
                          <span className="text-sm">
                            <span className="font-semibold">#{sticker.id}</span> - {sticker.name}
                          </span>
                          <button
                            onClick={() => setSelectedStickerIds(prev => prev.filter(id => id !== sticker.id))}
                            className="text-red-600 hover:text-red-800 text-sm font-medium"
                            type="button"
                          >
                            Remove
                          </button>
                        </div>
                      ))}
                  </div>
                ) : (
                  <p className="text-gray-500 text-sm text-center py-4">No stickers in this album</p>
                )}
              </div>
            </div>

            {/* Available Stickers */}
            {availableStickers.length > 0 && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Add Stickers (without albums) ({availableStickers.length})
                </label>
                <div className="border border-gray-300 rounded-lg p-3 max-h-48 overflow-y-auto bg-gray-50">
                  <div className="space-y-2">
                    {availableStickers.map(sticker => (
                      <div key={sticker.id} className="flex items-center justify-between bg-white p-2 rounded shadow-sm">
                        <span className="text-sm">
                          <span className="font-semibold">#{sticker.id}</span> - {sticker.name}
                        </span>
                        <button
                          onClick={() => setSelectedStickerIds(prev => [...prev, sticker.id])}
                          disabled={selectedStickerIds.includes(sticker.id)}
                          className={`text-sm font-medium ${
                            selectedStickerIds.includes(sticker.id)
                              ? 'text-gray-400 cursor-not-allowed'
                              : 'text-green-600 hover:text-green-800'
                          }`}
                          type="button"
                        >
                          {selectedStickerIds.includes(sticker.id) ? 'Added' : 'Add'}
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}
          </div>
        </FormModal>

        {/* Delete Confirmation Modal */}
        <DeleteConfirmModal
          isOpen={showDeleteModal}
          title="Delete Album"
          message={`Are you sure you want to delete "${selectedAlbum?.name}"? This action cannot be undone.`}
          onConfirm={handleConfirmDelete}
          onCancel={() => setShowDeleteModal(false)}
          isDeleting={isSubmitting}
        />
      </div>
    </div>
  );
};

export default AlbumManagement;
