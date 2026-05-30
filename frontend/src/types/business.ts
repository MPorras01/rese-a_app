export interface BusinessDto {
  id: string;
  ownerId: string;
  ownerName: string;
  name: string;
  description: string | null;
  category: string;
  address: string | null;
  city: string | null;
  phone: string | null;
  email: string | null;
  website: string | null;
  status: string;
  rejectionReason: string | null;
  createdAt: string;
  updatedAt: string;
  avgRating: number | null;
  totalReviews?: number;
  lat?: number | null;
  lng?: number | null;
}

export interface ProductDto {
  id: string;
  name: string;
  description: string | null;
  priceRange: string | null;
  active?: boolean;
}

export interface ReviewDto {
  id: string;
  userId?: string;
  userName?: string;
  rating: number;
  body: string;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
}
