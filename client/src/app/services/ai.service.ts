import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface AiChatResponse {
  response: string;
  matchedVacations: string[];
}

export interface AiSearchResult {
  vacationId: number;
  title: string;
  price: number;
  imageUrl: string;
  content: string;
}

export interface AiSearchResponse {
  query: string;
  results: AiSearchResult[];
  count: number;
}


@Injectable({ providedIn: 'root' })
export class AiService {
  private apiUrl = environment.URL + '/api/ai';

  constructor(private http: HttpClient) {}

  chat(message: string): Observable<AiChatResponse> {
    return this.http.post<AiChatResponse>(`${this.apiUrl}/chat`, { message });
  }


  search(query: string, limit: number = 5): Observable<AiSearchResponse> {
    return this.http.get<AiSearchResponse>(`${this.apiUrl}/search`, {
      params: { query, limit: limit.toString() }
    });
  }


  ingestVacations(): Observable<{ message: string; count: number }> {
    return this.http.post<{ message: string; count: number }>(`${this.apiUrl}/ingest`, {});
  }


  healthCheck(): Observable<{ status: string; vectorStore: string; chatModel: string }> {
    return this.http.get<{ status: string; vectorStore: string; chatModel: string }>(`${this.apiUrl}/health`);
  }
}
