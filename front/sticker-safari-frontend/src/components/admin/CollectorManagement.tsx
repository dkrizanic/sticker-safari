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

  // Pagination state
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalItems, setTotalItems] = useState(0);
  const [sortBy, setSortBy] = useState('id');
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('asc');
  
  // Search state
  const [searchQuery, setSearchQuery] = useState('');
  const [searchInput, setSearchInput] = useState('');

  useEffect(() => {
    loadCollectors();
  }, [currentPage, pageSize, sortBy, sortDir, searchQuery]);

  const loadCollectors = async () => {
    try {
      setLoading(true);
      const data = await apiService.getCollectorsPaginated(
        currentPage,
        pageSize,
        sortBy,
        sortDir,
        searchQuery
      );
      setCollectors(data.collectors);
      setTotalPages(data.totalPages);
      setTotalItems(data.totalItems);
    } catch (error) {
      addToast('Failed to load collectors', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = () => {
    setSearchQuery(searchInput);
    setCurrentPage(0);
  };

  const handleClearSearch = () => {
    setSearchInput('');
    setSearchQuery('');
    setCurrentPage(0);
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

        {/* Search and Sort Controls */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Search Bar */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Search by Username
              </label>
              <div className="flex gap-2">
                <input
                  type="text"
                  value={searchInput}
                  onChange={(e) => setSearchInput(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                  placeholder="Search collectors by username..."
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

            {/* Sort Controls */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Sort by
              </label>
              <div className="flex gap-2">
                {['id', 'username'].map(field => (
                  <button
                    key={field}
                    onClick={() => handleSortChange(field)}
                    className={`px-4 py-2 rounded-lg text-sm ${
                      sortBy === field
                        ? 'bg-blue-600 text-white'
                        : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                    }`}
                  >
                    {field === 'id' ? 'ID' : 'Username'}
                    {sortBy === field && (
                      <span className="ml-1">{sortDir === 'asc' ? '↑' : '↓'}</span>
                    )}
                  </button>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Results Info */}
        <div className="mb-4 text-gray-600">
          {searchQuery && (
            <span className="mr-4">
              Search results for: <strong>"{searchQuery}"</strong>
            </span>
          )}
          Showing {collectors.length} of {totalItems} collectors
        </div>

        <AdminTable
          columns={columns}
          data={collectors}
          onEdit={handleEdit}
          onDelete={handleDelete}
          emptyMessage="No collectors found. Try adjusting your search."
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
