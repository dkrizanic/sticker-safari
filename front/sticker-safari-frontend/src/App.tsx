import './App.css';
import Header from './components/Header';
import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import Collection from "./components/Collection";
import Login from "./components/Login";
import Trading from "./components/Trading"
import PackOpening from "./components/PackOpening"
import LandingPage from "./components/LandingPage"
import AdminDashboard from "./components/AdminDashboard"
import AlbumManagement from "./components/admin/AlbumManagement"
import StickerManagement from "./components/admin/StickerManagement"
import CollectorManagement from "./components/admin/CollectorManagement"
import Profile from "./components/Profile"
import { UserProvider, useUser } from './context/UserContext';
import { ToastProvider } from './context/ToastContext';
import ToastContainer from './components/ToastContainer';
import { useToast } from './context/ToastContext';

function AppContent(): JSX.Element {
  const { currentUser } = useUser();
  const { toasts, removeToast } = useToast();
  
  const getDefaultRoute = () => {
    if (!currentUser) return <LandingPage />;
    if (currentUser.role === 'ADMIN') return <Navigate to="/admin" replace />;
    return <Navigate to="/collection" replace />;
  };

  return (
    <div>
      {currentUser && <Header />}
      <ToastContainer toasts={toasts} removeToast={removeToast} />
      <Routes>
        <Route path="/" element={getDefaultRoute()} />
        <Route path="/collection" element={<Collection/>} />
        <Route path="/login" element={<Login/>} />
        <Route path="/trading" element={<Trading/>} />
        <Route path="/pack-opening" element={<PackOpening/>}/>
        <Route path="/profile" element={<Profile/>}/>
        <Route path="/admin" element={<AdminDashboard/>}/>
        <Route path="/admin/albums" element={<AlbumManagement/>}/>
        <Route path="/admin/stickers" element={<StickerManagement/>}/>
        <Route path="/admin/collectors" element={<CollectorManagement/>}/>
      </Routes>
    </div>
  );
}

function App(): JSX.Element {
  return (
    <UserProvider>
      <ToastProvider>
        <Router>
          <AppContent />
        </Router>
      </ToastProvider>
    </UserProvider>
  )
}

export default App;
