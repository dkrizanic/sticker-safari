import React from 'react';
import { useNavigate } from 'react-router-dom';

const LandingPage: React.FC = () => {
    const navigate = useNavigate();

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-500 via-purple-500 to-pink-500">
            {/* Navigation Bar */}
            <nav className="bg-white/10 backdrop-blur-md shadow-lg">
                <div className="container mx-auto px-4 py-4 flex justify-between items-center">
                    <div className="text-white text-2xl font-bold flex items-center space-x-2">
                        <span>🦁</span>
                        <span>Sticker Safari</span>
                    </div>
                    <div className="space-x-3">
                        <button
                            onClick={() => navigate('/login')}
                            className="bg-white text-purple-600 px-6 py-2 rounded-full font-semibold hover:bg-gray-100 transition-all shadow-md"
                        >
                            Login
                        </button>
                        <button
                            onClick={() => navigate('/login')}
                            className="bg-purple-600 text-white px-6 py-2 rounded-full font-semibold hover:bg-purple-700 transition-all shadow-md border-2 border-white"
                        >
                            Register
                        </button>
                    </div>
                </div>
            </nav>
            
            {/* Hero Section */}
            <div className="container mx-auto px-4 py-16">
                <div className="text-center mb-16">
                    <h1 className="text-6xl font-bold text-white mb-6 drop-shadow-lg">
                        🦁 Sticker Safari 🐘
                    </h1>
                    <p className="text-2xl text-white/90 mb-8">
                        Collect, Trade, and Complete Your Wildlife Album!
                    </p>
                    <button
                        onClick={() => navigate('/login')}
                        className="bg-white text-purple-600 px-8 py-4 rounded-full text-xl font-bold hover:bg-gray-100 transform hover:scale-105 transition-all shadow-lg"
                    >
                        Get Started →
                    </button>
                </div>

                {/* Features Grid */}
                <div className="grid md:grid-cols-3 gap-8 mt-16">
                    <div className="bg-white/10 backdrop-blur-lg rounded-2xl p-8 text-white hover:bg-white/20 transition-all transform hover:-translate-y-2">
                        <div className="text-6xl mb-4">📦</div>
                        <h3 className="text-2xl font-bold mb-3">Open Packs</h3>
                        <p className="text-white/80">
                            Open exciting sticker packs and discover rare wildlife stickers from around the world!
                        </p>
                    </div>

                    <div className="bg-white/10 backdrop-blur-lg rounded-2xl p-8 text-white hover:bg-white/20 transition-all transform hover:-translate-y-2">
                        <div className="text-6xl mb-4">🔄</div>
                        <h3 className="text-2xl font-bold mb-3">Trade Stickers</h3>
                        <p className="text-white/80">
                            Exchange duplicates with other collectors and complete your albums faster!
                        </p>
                    </div>

                    <div className="bg-white/10 backdrop-blur-lg rounded-2xl p-8 text-white hover:bg-white/20 transition-all transform hover:-translate-y-2">
                        <div className="text-6xl mb-4">📚</div>
                        <h3 className="text-2xl font-bold mb-3">Complete Albums</h3>
                        <p className="text-white/80">
                            Fill your albums with amazing wildlife stickers and become the ultimate collector!
                        </p>
                    </div>
                </div>

                {/* Stats Section */}
                <div className="mt-20 bg-white/10 backdrop-blur-lg rounded-2xl p-12 text-white">
                    <div className="grid md:grid-cols-3 gap-8 text-center">
                        <div>
                            <div className="text-5xl font-bold mb-2">100+</div>
                            <div className="text-xl text-white/80">Unique Stickers</div>
                        </div>
                        <div>
                            <div className="text-5xl font-bold mb-2">10+</div>
                            <div className="text-xl text-white/80">Albums to Complete</div>
                        </div>
                        <div>
                            <div className="text-5xl font-bold mb-2">∞</div>
                            <div className="text-xl text-white/80">Trading Possibilities</div>
                        </div>
                    </div>
                </div>

                {/* CTA Section */}
                <div className="mt-16 text-center">
                    <h2 className="text-4xl font-bold text-white mb-6">
                        Ready to Start Your Collection?
                    </h2>
                    <div className="space-x-4">
                        <button
                            onClick={() => navigate('/login')}
                            className="bg-white text-purple-600 px-8 py-4 rounded-full text-lg font-bold hover:bg-gray-100 transform hover:scale-105 transition-all shadow-lg"
                        >
                            Login
                        </button>
                        <button
                            onClick={() => navigate('/login')}
                            className="bg-purple-600 text-white px-8 py-4 rounded-full text-lg font-bold hover:bg-purple-700 transform hover:scale-105 transition-all shadow-lg border-2 border-white"
                        >
                            Register
                        </button>
                    </div>
                </div>

                {/* Footer */}
                <div className="mt-20 text-center text-white/60">
                    <p className="text-sm">
                        © 2026 Sticker Safari. Collect them all! 🌍
                    </p>
                </div>
            </div>
        </div>
    );
};

export default LandingPage;
