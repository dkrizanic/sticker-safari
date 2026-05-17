import React, { useState, useEffect } from 'react';
import apiService, { RankingEntry, PagedRankingResponse } from '../api/apiService';
import { useToast } from '../context/ToastContext';

type RankingType = 'all-time' | 'monthly' | 'yearly';

const Leaderboard = (): JSX.Element => {
    const [rankings, setRankings] = useState<RankingEntry[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [rankingType, setRankingType] = useState<RankingType>('all-time');
    const [selectedYear, setSelectedYear] = useState<number>(new Date().getFullYear());
    const [selectedMonth, setSelectedMonth] = useState<number>(new Date().getMonth() + 1);
    const [searchTerm, setSearchTerm] = useState<string>('');
    const [currentPage, setCurrentPage] = useState<number>(0); // Backend uses 0-based pages
    const [totalPages, setTotalPages] = useState<number>(0);
    const [totalItems, setTotalItems] = useState<number>(0);
    const { addToast } = useToast();

    const itemsPerPage = 10;

    const currentYear = new Date().getFullYear();
    const years = Array.from({ length: 5 }, (_, i) => currentYear - i);
    const months = [
        { value: 1, name: 'January' }, { value: 2, name: 'February' }, { value: 3, name: 'March' },
        { value: 4, name: 'April' }, { value: 5, name: 'May' }, { value: 6, name: 'June' },
        { value: 7, name: 'July' }, { value: 8, name: 'August' }, { value: 9, name: 'September' },
        { value: 10, name: 'October' }, { value: 11, name: 'November' }, { value: 12, name: 'December' }
    ];

    useEffect(() => {
        fetchRankings();
    }, [rankingType, selectedYear, selectedMonth, currentPage, searchTerm]);

    useEffect(() => {
        setCurrentPage(0); // Reset to page 0 when filters change
    }, [rankingType, selectedYear, selectedMonth, searchTerm]);

    const fetchRankings = async () => {
        setLoading(true);
        try {
            let data: PagedRankingResponse;
            
            switch (rankingType) {
                case 'all-time':
                    data = await apiService.getAllTimeRanking(currentPage, itemsPerPage, searchTerm || undefined);
                    break;
                case 'yearly':
                    data = await apiService.getYearlyRanking(selectedYear, currentPage, itemsPerPage, searchTerm || undefined);
                    break;
                case 'monthly':
                    data = await apiService.getMonthlyRanking(selectedYear, selectedMonth, currentPage, itemsPerPage, searchTerm || undefined);
                    break;
                default:
                    data = { rankings: [], currentPage: 0, totalPages: 0, totalItems: 0, pageSize: itemsPerPage };
            }
            
            setRankings(data.rankings);
            setTotalPages(data.totalPages);
            setTotalItems(data.totalItems);
        } catch (error) {
            console.error('Failed to fetch rankings:', error);
            addToast('Failed to load rankings', 'error');
        } finally {
            setLoading(false);
        }
    };

    const getRankingTitle = () => {
        switch (rankingType) {
            case 'all-time':
                return 'All-Time Top Collectors';
            case 'yearly':
                return `Top Collectors of ${selectedYear}`;
            case 'monthly':
                return `Top Collectors of ${months[selectedMonth - 1].name} ${selectedYear}`;
            default:
                return 'Leaderboard';
        }
    };

    const getMedalEmoji = (index: number) => {
        const actualRank = currentPage * itemsPerPage + index;
        switch (actualRank) {
            case 0: return '🥇';
            case 1: return '🥈';
            case 2: return '🥉';
            default: return `${actualRank + 1}.`;
        }
    };

    const goToPage = (page: number) => {
        if (page >= 0 && page < totalPages) {
            setCurrentPage(page);
        }
    };

    const getPageNumbers = () => {
        const pages: number[] = [];
        const maxPagesToShow = 5;
        
        if (totalPages <= maxPagesToShow) {
            for (let i = 0; i < totalPages; i++) {
                pages.push(i);
            }
        } else {
            if (currentPage <= 2) {
                pages.push(0, 1, 2, 3, totalPages - 1);
            } else if (currentPage >= totalPages - 3) {
                pages.push(0, totalPages - 4, totalPages - 3, totalPages - 2, totalPages - 1);
            } else {
                pages.push(0, currentPage - 1, currentPage, currentPage + 1, totalPages - 1);
            }
        }
        
        return pages;
    };

    return (
        <div className="min-h-screen bg-gray-100 py-8 px-4">
            <div className="max-w-4xl mx-auto">
                <div className="bg-white rounded-lg shadow-md p-6">
                    <h1 className="text-3xl font-bold text-gray-800 mb-6 text-center">
                        🏆 Leaderboard
                    </h1>

                    {/* Scoring Info Box */}
                    <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
                        <h3 className="font-semibold text-blue-900 mb-2">📊 Scoring System:</h3>
                        <div className="text-sm text-blue-800 space-y-1">
                            <p>⭐ <strong>Star</strong> = 5 points (Only first 3 collectors to complete each album)</p>
                            <p>📚 <strong>Album Completion</strong> = 1 point (Everyone who completes)</p>
                            <p>🏆 <strong>Total Score</strong> = (Stars × 5) + (Completions × 1)</p>
                        </div>
                    </div>

                    {/* Ranking Type Selector */}
                    <div className="mb-6 flex flex-wrap gap-3">
                        <button
                            onClick={() => setRankingType('all-time')}
                            className={`px-4 py-2 rounded-lg transition-colors ${
                                rankingType === 'all-time'
                                    ? 'bg-blue-500 text-white'
                                    : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                            }`}
                        >
                            All-Time
                        </button>
                        <button
                            onClick={() => setRankingType('yearly')}
                            className={`px-4 py-2 rounded-lg transition-colors ${
                                rankingType === 'yearly'
                                    ? 'bg-blue-500 text-white'
                                    : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                            }`}
                        >
                            Yearly
                        </button>
                        <button
                            onClick={() => setRankingType('monthly')}
                            className={`px-4 py-2 rounded-lg transition-colors ${
                                rankingType === 'monthly'
                                    ? 'bg-blue-500 text-white'
                                    : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                            }`}
                        >
                            Monthly
                        </button>
                    </div>

                    {/* Date Selectors */}
                    {(rankingType === 'yearly' || rankingType === 'monthly') && (
                        <div className="mb-6 flex gap-3">
                            <select
                                value={selectedYear}
                                onChange={(e) => setSelectedYear(parseInt(e.target.value))}
                                className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                            >
                                {years.map(year => (
                                    <option key={year} value={year}>{year}</option>
                                ))}
                            </select>

                            {rankingType === 'monthly' && (
                                <select
                                    value={selectedMonth}
                                    onChange={(e) => setSelectedMonth(parseInt(e.target.value))}
                                    className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                                >
                                    {months.map(month => (
                                        <option key={month.value} value={month.value}>
                                            {month.name}
                                        </option>
                                    ))}
                                </select>
                            )}
                        </div>
                    )}

                    {/* Search Box */}
                    <div className="mb-6">
                        <div className="relative">
                            <input
                                type="text"
                                placeholder="🔍 Search collectors by username..."
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                                className="w-full px-4 py-3 pl-10 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                            />
                            <svg
                                className="absolute left-3 top-3.5 h-5 w-5 text-gray-400"
                                fill="none"
                                stroke="currentColor"
                                viewBox="0 0 24 24"
                            >
                                <path
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    strokeWidth={2}
                                    d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                                />
                            </svg>
                            {searchTerm && (
                                <button
                                    onClick={() => setSearchTerm('')}
                                    className="absolute right-3 top-3 text-gray-400 hover:text-gray-600"
                                >
                                    ✕
                                </button>
                            )}
                        </div>
                        {searchTerm && (
                            <p className="mt-2 text-sm text-gray-600">
                                Found {totalItems} collector{totalItems !== 1 ? 's' : ''}
                            </p>
                        )}
                    </div>

                    {/* Rankings Title */}
                    <h2 className="text-2xl font-semibold text-gray-700 mb-4 text-center">
                        {getRankingTitle()}
                    </h2>

                    {/* Rankings Table */}
                    {loading ? (
                        <div className="text-center py-8">
                            <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
                            <p className="mt-4 text-gray-600">Loading rankings...</p>
                        </div>
                    ) : rankings.length === 0 ? (
                        <div className="text-center py-8">
                            <p className="text-gray-600">
                                {searchTerm ? `No collectors found matching "${searchTerm}"` : 'No rankings available for this period.'}
                            </p>
                        </div>
                    ) : (
                        <>
                            <div className="overflow-x-auto">
                                <table className="w-full">
                                    <thead>
                                        <tr className="border-b-2 border-gray-300">
                                            <th className="py-3 px-4 text-left">Rank</th>
                                            <th className="py-3 px-4 text-left">Collector</th>
                                            {rankingType === 'all-time' ? (
                                                <>
                                                    <th className="py-3 px-4 text-right">⭐ Stars (×5)</th>
                                                    <th className="py-3 px-4 text-right">📚 Albums (×1)</th>
                                                    <th className="py-3 px-4 text-right">🏆 Score</th>
                                                </>
                                            ) : (
                                                <th className="py-3 px-4 text-right">Completions</th>
                                            )}
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {rankings.map((entry, index) => {
                                            const actualRank = currentPage * itemsPerPage + index;
                                            return (
                                                <tr
                                                    key={entry.collectorId}
                                                    className={`border-b border-gray-200 hover:bg-gray-50 ${
                                                        actualRank < 3 ? 'bg-yellow-50' : ''
                                                    }`}
                                                >
                                                    <td className="py-3 px-4 font-semibold text-lg">
                                                        {getMedalEmoji(index)}
                                                    </td>
                                                    <td className="py-3 px-4 font-medium">
                                                        {entry.username}
                                                    </td>
                                                    {rankingType === 'all-time' ? (
                                                        <>
                                                            <td className="py-3 px-4 text-right font-semibold">
                                                                {entry.stars || 0}
                                                            </td>
                                                            <td className="py-3 px-4 text-right font-semibold">
                                                                {entry.completions || 0}
                                                            </td>
                                                            <td className="py-3 px-4 text-right font-bold text-lg text-blue-600">
                                                                {entry.score || 0}
                                                            </td>
                                                        </>
                                                    ) : (
                                                        <td className="py-3 px-4 text-right font-semibold text-lg">
                                                            {entry.completions}
                                                        </td>
                                                    )}
                                                </tr>
                                            );
                                        })}
                                    </tbody>
                                </table>
                            </div>

                            {/* Pagination Controls */}
                            {totalPages > 1 && (
                                <div className="mt-6 flex items-center justify-center gap-2">
                                    <button
                                        onClick={() => goToPage(currentPage - 1)}
                                        disabled={currentPage === 0}
                                        className={`px-4 py-2 rounded-lg transition-colors ${
                                            currentPage === 0
                                                ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                                                : 'bg-blue-500 text-white hover:bg-blue-600'
                                        }`}
                                    >
                                        ← Previous
                                    </button>

                                    <div className="flex gap-1">
                                        {getPageNumbers().map((page, idx) => {
                                            const prevPage = getPageNumbers()[idx - 1];
                                            const showEllipsis = prevPage !== undefined && page - prevPage > 1;

                                            return (
                                                <React.Fragment key={page}>
                                                    {showEllipsis && (
                                                        <span className="px-3 py-2 text-gray-500">...</span>
                                                    )}
                                                    <button
                                                        onClick={() => goToPage(page)}
                                                        className={`px-4 py-2 rounded-lg transition-colors ${
                                                            currentPage === page
                                                                ? 'bg-blue-500 text-white'
                                                                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                                                        }`}
                                                    >
                                                        {page + 1}
                                                    </button>
                                                </React.Fragment>
                                            );
                                        })}
                                    </div>

                                    <button
                                        onClick={() => goToPage(currentPage + 1)}
                                        disabled={currentPage === totalPages - 1}
                                        className={`px-4 py-2 rounded-lg transition-colors ${
                                            currentPage === totalPages - 1
                                                ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                                                : 'bg-blue-500 text-white hover:bg-blue-600'
                                        }`}
                                    >
                                        Next →
                                    </button>
                                </div>
                            )}

                            {/* Pagination Info */}
                            {totalPages > 1 && (
                                <p className="mt-4 text-center text-sm text-gray-600">
                                    Showing {currentPage * itemsPerPage + 1}-{Math.min((currentPage + 1) * itemsPerPage, totalItems)} of {totalItems} collectors
                                </p>
                            )}
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default Leaderboard;
