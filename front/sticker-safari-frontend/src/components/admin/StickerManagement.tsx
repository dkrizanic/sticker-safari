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
  albumId?: number; // Optional: album to assign sticker to
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
  
  // Pagination state
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalItems, setTotalItems] = useState(0);
  const [sortBy, setSortBy] = useState('id');
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('asc');
  
  // Filter state
  const [searchQuery, setSearchQuery] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [selectedAlbumId, setSelectedAlbumId] = useState<number | undefined>(undefined);

  useEffect(() => {
    loadAlbums();
  }, []);

  useEffect(() => {
    loadStickers();
  }, [currentPage, pageSize, sortBy, sortDir, searchQuery, selectedAlbumId]);

  const loadAlbums = async () => {
    try {
      const albumsData = await apiService.getAllAlbums();
      setAlbums(albumsData);
    } catch (error) {
      addToast('Failed to load albums', 'error');
    }
  };

  const loadStickers = async () => {
    try {
      setLoading(true);
      const data = await apiService.getStickersPaginated(
        currentPage,
        pageSize,
        sortBy,
        sortDir,
        searchQuery,
        selectedAlbumId
      );
      setStickers(data.stickers);
      setTotalPages(data.totalPages);
      setTotalItems(data.totalItems);
    } catch (error) {
      addToast('Failed to load stickers', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = () => {
    setSearchQuery(searchInput);
    setCurrentPage(0); // Reset to first page when searching
  };

  const handleClearSearch = () => {
    setSearchInput('');
    setSearchQuery('');
    setCurrentPage(0);
  };

  const handleAlbumFilterChange = (albumId: string) => {
    setSelectedAlbumId(albumId ? Number(albumId) : undefined);
    setCurrentPage(0); // Reset to first page when filtering
  };

  const handleSortChange = (field: string) => {
    if (sortBy === field) {
      setSortDir(sortDir === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(field);
      setSortDir('asc');
    }
    setCurrentPage(0);
  };

  const handlePageSizeChange = (newSize: number) => {
    setPageSize(newSize);
    setCurrentPage(0);
  };

  const handleCreate = () => {
    setFormData({
      name: '',
      latinName: '',
      description: '',
      habitat: '',
      src: '',
      albumId: undefined
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
      src: sticker.src,
      albumId: sticker.albumId // Album cannot be changed during edit
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
      loadStickers();
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
      loadStickers();
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
          </div>
          <button
            onClick={handleCreate}
            className="px-6 py-3 bg-gradient-to-r from-purple-600 to-blue-600 text-white rounded-lg hover:from-purple-700 hover:to-blue-700 transition-all shadow-lg"
          >
            + Create Sticker
          </button>
        </div>

        {/* Search and Filter Controls */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {/* Search Bar */}
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Smart Search
              </label>
              <div className="flex gap-2">
                <input
                  type="text"
                  value={searchInput}
                  onChange={(e) => setSearchInput(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                  placeholder="Search by name, latin name, description, or habitat..."
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <button
                  onClick={handleSearch}
                  className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                >
                  Search
                </button>
                {searchQuery && (
                  <button
                    onClick={handleClearSearch}
                    className="px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors"
                  >
                    Clear
                  </button>
                )}
              </div>
            </div>

            {/* Album Filter */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Filter by Album
              </label>
              <select
                value={selectedAlbumId || ''}
                onChange={(e) => handleAlbumFilterChange(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">All Albums</option>
                {albums.map(album => (
                  <option key={album.id} value={album.id}>
                    {album.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Sort Controls */}
          <div className="mt-4 flex items-center gap-4">
            <span className="text-sm font-medium text-gray-700">Sort by:</span>
            {['id', 'name', 'latinName', 'habitat'].map(field => (
              <button
                key={field}
                onClick={() => handleSortChange(field)}
                className={`px-3 py-1 rounded-lg text-sm ${
                  sortBy === field
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                }`}
              >
                {field === 'id' ? 'ID' : field.charAt(0).toUpperCase() + field.slice(1)}
                {sortBy === field && (
                  <span className="ml-1">{sortDir === 'asc' ? '↑' : '↓'}</span>
                )}
              </button>
            ))}
          </div>
        </div>

        {/* Results Info */}
        <div className="mb-4 text-gray-600">
          {searchQuery && (
            <span className="mr-4">
              Search results for: <strong>"{searchQuery}"</strong>
            </span>
          )}
          Showing {stickers.length} of {totalItems} stickers
        </div>

        <AdminTable
          columns={columns}
          data={stickers}
          onEdit={handleEdit}
          emptyMessage="No stickers found. Try adjusting your search or filters."
        />

        {/* Pagination Controls */}
        {totalPages > 0 && (
          <div className="mt-6 flex items-center justify-between bg-white rounded-lg shadow-md p-4">
            <div className="flex items-center gap-4">
              <span className="text-sm text-gray-700">Items per page:</span>
              <select
                value={pageSize}
                onChange={(e) => handlePageSizeChange(Number(e.target.value))}
                className="px-3 py-1 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value={5}>5</option>
                <option value={10}>10</option>
                <option value={20}>20</option>
                <option value={50}>50</option>
              </select>
            </div>

            <div className="flex items-center gap-2">
              <button
                onClick={() => setCurrentPage(0)}
                disabled={currentPage === 0}
                className="px-3 py-1 rounded-lg bg-gray-200 text-gray-700 hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                First
              </button>
              <button
                onClick={() => setCurrentPage(currentPage - 1)}
                disabled={currentPage === 0}
                className="px-3 py-1 rounded-lg bg-gray-200 text-gray-700 hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Previous
              </button>
              <span className="px-4 py-1 text-gray-700">
                Page {currentPage + 1} of {totalPages}
              </span>
              <button
                onClick={() => setCurrentPage(currentPage + 1)}
                disabled={currentPage >= totalPages - 1}
                className="px-3 py-1 rounded-lg bg-gray-200 text-gray-700 hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Next
              </button>
              <button
                onClick={() => setCurrentPage(totalPages - 1)}
                disabled={currentPage >= totalPages - 1}
                className="px-3 py-1 rounded-lg bg-gray-200 text-gray-700 hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                Last
              </button>
            </div>
          </div>
        )}

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
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Album (Optional)
            </label>
            <select
              value={formData.albumId || ''}
              onChange={(e) => setFormData({ ...formData, albumId: e.target.value ? Number(e.target.value) : undefined })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">-- No Album --</option>
              {albums.map(album => (
                <option key={album.id} value={album.id}>
                  {album.name} (ID: {album.id})
                </option>
              ))}
            </select>
            <p className="text-xs text-gray-500 mt-1">You can assign the sticker to an album now, or leave it unassigned and add it to an album later.</p>
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
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Album
            </label>
            <input
              type="text"
              value={formData.albumId ? albums.find(a => a.id === formData.albumId)?.name || 'Unknown Album' : 'No Album'}
              disabled
              className="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-100 text-gray-500 cursor-not-allowed"
            />
            <p className="text-xs text-gray-500 mt-1">⚠️ Album assignment cannot be changed after creation (per requirements).</p>
          </div>
        </FormModal>
      </div>
    </div>
  );
};

export default StickerManagement;
